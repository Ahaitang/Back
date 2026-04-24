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
import java.net.URLEncoder;
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
        OcrResult result = paddleOcrVl(file);

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
    public OcrResult paddleOcrVl(MultipartFile file) {
        try {
            String token = getAccessToken();
            if (token == null) {
                OcrResult ocrResult = new OcrResult();
                ocrResult.setSuccess(false);
                ocrResult.setErrorMsg("获取Access Token失败，请检查百度OCR配置");
                return ocrResult;
            }

            // Step 1: 提交任务
            String taskId = submitVlParserTask(token, file);
            if (taskId == null) {
                OcrResult ocrResult = new OcrResult();
                ocrResult.setSuccess(false);
                ocrResult.setErrorMsg("提交文档解析任务失败");
                return ocrResult;
            }

            log.info("PaddleOCR-VL任务提交成功，taskId: {}", taskId);

            // Step 2: 轮询查询结果（最多等待60秒，建议5-10秒后开始轮询）
            Thread.sleep(5000); // 先等待5秒
            int maxRetries = 12;
            int retryInterval = 5000;
            for (int i = 0; i < maxRetries; i++) {
                OcrResult queryResult = queryVlParserTask(token, taskId);
                if (queryResult != null) {
                    // 返回结果（成功或失败）
                    return queryResult;
                }
                Thread.sleep(retryInterval);
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
     * 使用 application/x-www-form-urlencoded 格式
     */
    private String submitVlParserTask(String token, MultipartFile file) throws Exception {
        String apiUrl = VL_PARSER_TASK_URL + "?access_token=" + token;

        byte[] imageData = file.getBytes();
        String imageBase64 = Base64.getEncoder().encodeToString(imageData);
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            fileName = "image.jpg";
        }

        // 构建 form-urlencoded 格式的请求体
        // 必选参数: file_data, file_name
        String formBody = "file_data=" + URLEncoder.encode(imageBase64, "UTF-8") +
                         "&file_name=" + URLEncoder.encode(fileName, "UTF-8");

        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setDoOutput(true);
        connection.setConnectTimeout(config.getConnectTimeout());
        connection.setReadTimeout(config.getReadTimeout());

        OutputStream os = connection.getOutputStream();
        os.write(formBody.getBytes("UTF-8"));
        os.close();

        int responseCode = connection.getResponseCode();
        InputStream is;
        if (responseCode >= 400) {
            is = connection.getErrorStream();
        } else {
            is = connection.getInputStream();
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        reader.close();

        log.debug("提交任务响应: {}", result.toString());

        JSONObject jsonResult = new JSONObject(result.toString());
        int errorCode = jsonResult.optInt("error_code", -1);
        if (errorCode != 0) {
            log.error("提交任务失败, error_code: {}, error_msg: {}", errorCode, jsonResult.optString("error_msg"));
            return null;
        }

        JSONObject resultObj = jsonResult.optJSONObject("result");
        if (resultObj != null) {
            return resultObj.optString("task_id");
        }
        return null;
    }

    /**
     * 查询PaddleOCR-VL文档解析任务结果
     * 使用 application/x-www-form-urlencoded 格式
     */
    private OcrResult queryVlParserTask(String token, String taskId) throws Exception {
        String apiUrl = VL_PARSER_QUERY_URL + "?access_token=" + token;

        // 构建 form-urlencoded 格式的请求体
        String formBody = "task_id=" + URLEncoder.encode(taskId, "UTF-8");

        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setDoOutput(true);
        connection.setConnectTimeout(config.getConnectTimeout());
        connection.setReadTimeout(config.getReadTimeout());

        OutputStream os = connection.getOutputStream();
        os.write(formBody.getBytes("UTF-8"));
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

        int errorCode = jsonResult.optInt("error_code", -1);
        if (errorCode != 0) {
            log.warn("查询任务失败, error_code: {}, error_msg: {}", errorCode, jsonResult.optString("error_msg"));
            return null;
        }

        JSONObject resultObj = jsonResult.optJSONObject("result");
        if (resultObj == null) {
            return null;
        }

        String status = resultObj.optString("status", "");
        log.debug("任务状态: {}", status);

        switch (status) {
            case "success":
                // 任务成功 - 获取 markdown_url 并下载内容
                String markdownUrl = resultObj.optString("markdown_url");
                if (markdownUrl != null && !markdownUrl.isEmpty()) {
                    String markdownContent = downloadMarkdown(markdownUrl);
                    OcrResult ocrResult = new OcrResult();
                    ocrResult.setSuccess(true);
                    ocrResult.setFullText(markdownContent);
                    if (markdownContent != null && !markdownContent.isEmpty()) {
                        ocrResult.setWords(Arrays.asList(markdownContent.split("\n")));
                    }
                    ocrResult.setSource("paddleocr-vl-cloud");
                    return ocrResult;
                }
                // 如果没有 markdown_url，返回失败
                OcrResult noResult = new OcrResult();
                noResult.setSuccess(false);
                noResult.setErrorMsg("解析结果为空");
                return noResult;

            case "failed":
                // 任务失败
                String taskError = resultObj.optString("task_error", "任务处理失败");
                OcrResult failedResult = new OcrResult();
                failedResult.setSuccess(false);
                failedResult.setErrorMsg(taskError);
                return failedResult;

            case "pending":
            case "processing":
            case "running":
                // 任务进行中，继续等待
                log.info("任务进行中，taskId: {}, status: {}", taskId, status);
                return null;

            default:
                log.warn("未知状态: {}", status);
                return null;
        }
    }

    /**
     * 下载 markdown 结果文件
     */
    private String downloadMarkdown(String url) {
        try {
            URL downloadUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(60000);

            if (connection.getResponseCode() != 200) {
                log.error("下载 markdown 失败，响应码: {}", connection.getResponseCode());
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            connection.disconnect();

            return content.toString();
        } catch (Exception e) {
            log.error("下载 markdown 内容失败", e);
            return null;
        }
    }

    /**
     * 解析通用OCR结果
     */
    private OcrResult parseResult(JSONObject result) {
        OcrResult ocrResult = new OcrResult();

        if (result.has("error_code")) {
            int errorCode = result.optInt("error_code");
            if (errorCode != 0) {
                ocrResult.setSuccess(false);
                ocrResult.setErrorMsg(result.optString("error_msg", "识别失败"));
                return ocrResult;
            }
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