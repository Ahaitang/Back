package org.hospital.ocr.service;

import com.baidu.aip.ocr.AipOcr;
import lombok.extern.slf4j.Slf4j;
import org.hospital.ocr.config.BaiduOcrConfig;
import org.hospital.ocr.dto.OcrResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * 百度OCR服务 - 病历解析
 */
@Slf4j
@Service
public class BaiduOcrService {

    @Autowired(required = false)
    private BaiduOcrConfig config;

    private AipOcr client;

    // PaddleOCR-VL 文档解析 API URL（异步任务模式）
    private static final String VL_PARSER_TASK_URL = "https://aip.baidubce.com/rest/2.0/brain/online/v2/paddle-vl-parser/task";
    private static final String VL_PARSER_QUERY_URL = "https://aip.baidubce.com/rest/2.0/brain/online/v2/paddle-vl-parser/task/query";

    // Access Token缓存
    private String accessToken;
    private long tokenExpireTime;

    @PostConstruct
    public void init() {
        if (config != null && config.getApiKey() != null && config.getSecretKey() != null) {
            client = new AipOcr(config.getAppId(), config.getApiKey(), config.getSecretKey());
            client.setConnectionTimeoutInMillis(config.getConnectTimeout());
            client.setSocketTimeoutInMillis(config.getReadTimeout());
            log.info("百度OCR客户端初始化成功");
        } else {
            log.warn("百度OCR配置缺失，OCR服务可能不可用");
        }
    }

    /**
     * 获取百度API Access Token
     */
    private String getAccessToken() {
        if (config == null) {
            return null;
        }

        // 检查token是否过期
        if (accessToken != null && tokenExpireTime > System.currentTimeMillis()) {
            return accessToken;
        }

        try {
            String tokenUrl = "https://aip.baidubce.com/oauth/2.0/token?" +
                    "grant_type=client_credentials" +
                    "&client_id=" + config.getApiKey() +
                    "&client_secret=" + config.getSecretKey();

            URL url = new URL(tokenUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(config.getConnectTimeout());
            connection.setReadTimeout(config.getReadTimeout());

            InputStream is = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(result.toString());
            accessToken = json.getString("access_token");
            tokenExpireTime = System.currentTimeMillis() + json.getLong("expires_in") * 1000 - 60000;

            log.info("获取百度Access Token成功");
            return accessToken;
        } catch (Exception e) {
            log.error("获取百度Access Token失败", e);
            return null;
        }
    }

    /**
     * 病历解析 - 先尝试 PaddleOCR-VL，失败后退回通用文字识别
     */
    public OcrResult accurateBasic(MultipartFile file) throws IOException {
        // 先尝试 PaddleOCR-VL（更适合病历等复杂文档）
        log.info("尝试使用 PaddleOCR-VL 识别病历");
        OcrResult result = paddleOcrVl(file, "识别病历图片内容，提取诊断、症状、用药、检查结果等关键医疗信息，保持原文格式");

        if (result != null && result.isSuccess() && result.getFullText() != null && !result.getFullText().isEmpty()) {
            log.info("PaddleOCR-VL 识别成功");
            return result;
        }

        // PaddleOCR-VL 失败，退回到百度通用OCR
        log.info("PaddleOCR-VL 识别失败，退回到百度通用OCR");
        if (client != null) {
            JSONObject baiduResult = client.accurateGeneral(file.getBytes(), new HashMap<>());
            OcrResult ocrResult = parseResult(baiduResult);
            if (ocrResult.isSuccess()) {
                ocrResult.setSource("baidu-ocr-fallback");
            }
            return ocrResult;
        }

        // 都失败了
        OcrResult failResult = new OcrResult();
        failResult.setSuccess(false);
        failResult.setErrorMsg("OCR识别失败");
        return failResult;
    }

    /**
     * 使用PaddleOCR-VL进行文档解析（免费API - 异步任务模式）
     */
    public OcrResult paddleOcrVl(MultipartFile file, String prompt) {
        try {
            String token = getAccessToken();
            if (token == null) {
                OcrResult ocrResult = new OcrResult();
                ocrResult.setSuccess(false);
                ocrResult.setErrorMsg("获取Access Token失败，请检查百度OCR配置");
                return ocrResult;
            }

            // Step 1: 提交任务
            String taskId = submitVlParserTask(token, file, prompt);
            if (taskId == null) {
                OcrResult ocrResult = new OcrResult();
                ocrResult.setSuccess(false);
                ocrResult.setErrorMsg("提交文档解析任务失败");
                return ocrResult;
            }

            log.info("PaddleOCR-VL任务提交成功，taskId: {}", taskId);

            // Step 2: 轮询查询结果（最多等待30秒）
            int maxRetries = 15;
            int retryInterval = 2000;
            for (int i = 0; i < maxRetries; i++) {
                Thread.sleep(retryInterval);
                OcrResult result = queryVlParserTask(token, taskId);
                if (result != null) {
                    return result;
                }
            }

            // 超时
            OcrResult ocrResult = new OcrResult();
            ocrResult.setSuccess(false);
            ocrResult.setErrorMsg("文档解析任务超时");
            return ocrResult;

        } catch (Exception e) {
            log.error("PaddleOCR-VL识别失败", e);
            OcrResult ocrResult = new OcrResult();
            ocrResult.setSuccess(false);
            ocrResult.setErrorMsg("识别失败: " + e.getMessage());
            return ocrResult;
        }
    }

    /**
     * 提交PaddleOCR-VL文档解析任务
     */
    private String submitVlParserTask(String token, MultipartFile file, String prompt) throws Exception {
        String apiUrl = VL_PARSER_TASK_URL + "?access_token=" + token;

        byte[] imageData = file.getBytes();
        String imageBase64 = Base64.getEncoder().encodeToString(imageData);

        JSONObject requestBody = new JSONObject();
        requestBody.put("image", imageBase64);
        if (prompt != null && !prompt.isEmpty()) {
            requestBody.put("prompt", prompt);
        }

        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.setConnectTimeout(config.getConnectTimeout());
        connection.setReadTimeout(config.getReadTimeout());

        OutputStream os = connection.getOutputStream();
        os.write(requestBody.toString().getBytes("UTF-8"));
        os.close();

        InputStream is = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        reader.close();

        JSONObject jsonResult = new JSONObject(result.toString());
        if (jsonResult.has("error_code")) {
            log.error("提交任务失败: {}", jsonResult.optString("error_msg"));
            return null;
        }

        return jsonResult.optString("task_id");
    }

    /**
     * 查询PaddleOCR-VL文档解析任务结果
     */
    private OcrResult queryVlParserTask(String token, String taskId) throws Exception {
        String apiUrl = VL_PARSER_QUERY_URL + "?access_token=" + token;

        JSONObject requestBody = new JSONObject();
        requestBody.put("task_id", taskId);

        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.setConnectTimeout(config.getConnectTimeout());
        connection.setReadTimeout(config.getReadTimeout());

        OutputStream os = connection.getOutputStream();
        os.write(requestBody.toString().getBytes("UTF-8"));
        os.close();

        InputStream is = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        reader.close();

        JSONObject jsonResult = new JSONObject(result.toString());

        if (jsonResult.has("error_code")) {
            log.warn("查询任务失败: {}", jsonResult.optString("error_msg"));
            return null;
        }

        int retCode = jsonResult.optInt("ret_code", -1);
        if (retCode == 0) {
            OcrResult ocrResult = new OcrResult();
            ocrResult.setSuccess(true);

            JSONObject resultData = jsonResult.optJSONObject("result");
            if (resultData != null) {
                String text = resultData.optString("text", "");
                ocrResult.setFullText(text);
                if (!text.isEmpty()) {
                    ocrResult.setWords(Arrays.asList(text.split("\n")));
                }
            }

            ocrResult.setSource("paddleocr-vl-cloud");
            return ocrResult;
        } else if (retCode == 1) {
            log.info("任务进行中，taskId: {}", taskId);
            return null;
        } else {
            log.error("任务失败，ret_code: {}, ret_msg: {}", retCode, jsonResult.optString("ret_msg"));
            OcrResult ocrResult = new OcrResult();
            ocrResult.setSuccess(false);
            ocrResult.setErrorMsg(jsonResult.optString("ret_msg", "任务处理失败"));
            return ocrResult;
        }
    }

    /**
     * 解析通用OCR结果
     */
    private OcrResult parseResult(JSONObject result) {
        OcrResult ocrResult = new OcrResult();

        if (result.has("error_code")) {
            ocrResult.setSuccess(false);
            ocrResult.setErrorMsg(result.optString("error_msg", "识别失败"));
            return ocrResult;
        }

        ocrResult.setSuccess(true);
        JSONArray wordsResult = result.optJSONArray("words_result");
        if (wordsResult != null) {
            List<String> words = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < wordsResult.length(); i++) {
                JSONObject item = wordsResult.optJSONObject(i);
                if (item != null) {
                    String word = item.optString("words", "");
                    words.add(word);
                    if (sb.length() > 0) {
                        sb.append("\n");
                    }
                    sb.append(word);
                }
            }
            ocrResult.setWords(words);
            ocrResult.setFullText(sb.toString());
        }

        ocrResult.setSource("baidu-ocr");
        return ocrResult;
    }
}