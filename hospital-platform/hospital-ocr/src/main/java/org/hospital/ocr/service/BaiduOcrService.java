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

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

/**
 * 百度OCR服务
 * 支持多种OCR类型识别，当API未开通时自动fallback到PaddleOCR-VL
 */
@Slf4j
@Service
public class BaiduOcrService {

    @Autowired
    private BaiduOcrConfig config;

    private AipOcr client;

    // 百度OCR API基础URL
    private static final String OCR_API_BASE = "https://aip.baidubce.com/rest/2.0/ocr/v1/";

    // PaddleOCR-VL 文档解析 API URL（异步任务模式）
    private static final String VL_PARSER_TASK_URL = "https://aip.baidubce.com/rest/2.0/brain/online/v2/paddle-vl-parser/task";
    private static final String VL_PARSER_QUERY_URL = "https://aip.baidubce.com/rest/2.0/brain/online/v2/paddle-vl-parser/task/query";

    // Access Token缓存
    private String accessToken;
    private long tokenExpireTime;

    @PostConstruct
    public void init() {
        client = new AipOcr(config.getAppId(), config.getApiKey(), config.getSecretKey());
        client.setConnectionTimeoutInMillis(config.getConnectTimeout());
        client.setSocketTimeoutInMillis(config.getReadTimeout());
    }

    /**
     * 获取百度API Access Token
     */
    private String getAccessToken() {
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
     * 使用PaddleOCR-VL进行文档解析（免费API - 异步任务模式）
     * 流程：提交任务 → 获取taskId → 轮询查询结果
     */
    public OcrResult paddleOcrVl(MultipartFile file, String prompt) {
        try {
            String token = getAccessToken();
            if (token == null) {
                OcrResult ocrResult = new OcrResult();
                ocrResult.setSuccess(false);
                ocrResult.setErrorMsg("获取Access Token失败");
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
            int retryInterval = 2000; // 2秒
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
            ocrResult.setErrorMsg("PaddleOCR-VL识别失败: " + e.getMessage());
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

        // 构建请求体
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

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            log.error("提交任务失败，响应码: {}", responseCode);
            return null;
        }

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

        // 构建请求体
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

        // 检查错误
        if (jsonResult.has("error_code")) {
            log.warn("查询任务失败: {}", jsonResult.optString("error_msg"));
            return null;
        }

        // 检查任务状态
        int retCode = jsonResult.optInt("ret_code", -1);
        if (retCode == 0) {
            // 任务完成
            OcrResult ocrResult = new OcrResult();
            ocrResult.setSuccess(true);

            JSONObject resultData = jsonResult.optJSONObject("result");
            if (resultData != null) {
                String text = resultData.optString("text", "");
                ocrResult.setFullText(text);
                if (!text.isEmpty()) {
                    String[] lines = text.split("\n");
                    ocrResult.setWords(Arrays.asList(lines));
                }

                // 解析结构化数据
                JSONObject structData = resultData.optJSONObject("struct_data");
                if (structData != null) {
                    Map<String, Object> dataMap = new HashMap<>();
                    for (String key : structData.keySet()) {
                        dataMap.put(key, structData.get(key));
                    }
                    ocrResult.setData(dataMap);
                }
            }

            ocrResult.setSource("paddleocr-vl-cloud");
            return ocrResult;
        } else if (retCode == 1) {
            // 任务进行中
            log.info("任务进行中，taskId: {}", taskId);
            return null;
        } else {
            // 任务失败
            log.error("任务失败，ret_code: {}, ret_msg: {}", retCode, jsonResult.optString("ret_msg"));
            OcrResult ocrResult = new OcrResult();
            ocrResult.setSuccess(false);
            ocrResult.setErrorMsg(jsonResult.optString("ret_msg", "任务处理失败"));
            return ocrResult;
        }
    }

    /**
     * 检查是否需要fallback到PaddleOCR-VL
     */
    private boolean needFallback(JSONObject result) {
        if (result.has("error_code")) {
            int errorCode = result.optInt("error_code");
            // 未开通服务的错误码
            return errorCode == 216100 || errorCode == 216101 || errorCode == 216102;
        }
        return false;
    }

    /**
     * 通用文字识别（标准版）
     */
    public OcrResult generalBasic(MultipartFile file) throws IOException {
        JSONObject result = client.basicGeneral(file.getBytes(), new HashMap<>());

        if (needFallback(result)) {
            log.info("通用文字识别API未开通，fallback到PaddleOCR-VL");
            return paddleOcrVl(file, "识别图片中的所有文字内容");
        }

        return parseResult(result);
    }

    /**
     * 通用文字识别（高精度版）
     */
    public OcrResult accurateBasic(MultipartFile file) throws IOException {
        JSONObject result = client.accurateGeneral(file.getBytes(), new HashMap<>());

        if (needFallback(result)) {
            log.info("高精度文字识别API未开通，fallback到PaddleOCR-VL");
            return paddleOcrVl(file, "精确识别图片中的所有文字内容");
        }

        return parseResult(result);
    }

    /**
     * 身份证识别
     */
    public OcrResult idCard(MultipartFile file, String side) throws IOException {
        HashMap<String, String> options = new HashMap<>();
        options.put("detect_direction", "true");
        options.put("detect_risk", "false");

        JSONObject result = client.idcard(file.getBytes(), side, options);

        if (needFallback(result)) {
            log.info("身份证识别API未开通，fallback到PaddleOCR-VL");
            String prompt = "front".equals(side)
                ? "识别身份证正面，提取姓名、性别、民族、出生日期、住址、公民身份号码"
                : "识别身份证反面，提取签发机关、签发日期、失效日期";
            return paddleOcrVl(file, prompt);
        }

        return parseIdCardResult(result, side);
    }

    /**
     * 银行卡识别
     */
    public OcrResult bankCard(MultipartFile file) throws IOException {
        JSONObject result = client.bankcard(file.getBytes(), new HashMap<>());

        if (needFallback(result)) {
            log.info("银行卡识别API未开通，fallback到PaddleOCR-VL");
            return paddleOcrVl(file, "识别银行卡，提取卡号、有效期、银行名称");
        }

        return parseBankCardResult(result);
    }

    /**
     * 驾驶证识别
     */
    public OcrResult drivingLicense(MultipartFile file) throws IOException {
        JSONObject result = client.drivingLicense(file.getBytes(), new HashMap<>());

        if (needFallback(result)) {
            log.info("驾驶证识别API未开通，fallback到PaddleOCR-VL");
            return paddleOcrVl(file, "识别驾驶证，提取证号、姓名、性别、国籍、住址、出生日期、初次领证日期、准驾车型、有效期起始日期、有效期截止日期");
        }

        return parseDrivingLicenseResult(result);
    }

    /**
     * 行驶证识别
     */
    public OcrResult vehicleLicense(MultipartFile file) throws IOException {
        JSONObject result = client.vehicleLicense(file.getBytes(), new HashMap<>());

        if (needFallback(result)) {
            log.info("行驶证识别API未开通，fallback到PaddleOCR-VL");
            return paddleOcrVl(file, "识别行驶证，提取车牌号码、车辆类型、所有人、品牌型号、车辆识别代号、发动机号码、注册日期、发证日期、使用性质");
        }

        return parseVehicleLicenseResult(result);
    }

    /**
     * 医疗发票识别
     */
    public OcrResult medicalInvoice(MultipartFile file) throws IOException {
        JSONObject result = client.medicalInvoice(file.getBytes(), new HashMap<>());

        if (needFallback(result)) {
            log.info("医疗发票识别API未开通，fallback到PaddleOCR-VL");
            return paddleOcrVl(file, "识别医疗发票，提取发票代码、发票号码、日期、金额、医院名称、患者姓名等关键信息");
        }

        return parseMedicalInvoiceResult(result);
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

        return ocrResult;
    }

    /**
     * 解析身份证识别结果
     */
    private OcrResult parseIdCardResult(JSONObject result, String side) {
        OcrResult ocrResult = new OcrResult();

        if (result.has("error_code")) {
            ocrResult.setSuccess(false);
            ocrResult.setErrorMsg(result.optString("error_msg", "识别失败"));
            return ocrResult;
        }

        ocrResult.setSuccess(true);
        JSONObject cardResult = result.optJSONObject("words_result");
        if (cardResult != null) {
            Map<String, String> data = new HashMap<>();

            if ("front".equals(side)) {
                // 正面
                data.put("姓名", getWords(cardResult, "姓名"));
                data.put("性别", getWords(cardResult, "性别"));
                data.put("民族", getWords(cardResult, "民族"));
                data.put("出生", getWords(cardResult, "出生"));
                data.put("住址", getWords(cardResult, "住址"));
                data.put("公民身份号码", getWords(cardResult, "公民身份号码"));
            } else {
                // 反面
                data.put("签发机关", getWords(cardResult, "签发机关"));
                data.put("签发日期", getWords(cardResult, "签发日期"));
                data.put("失效日期", getWords(cardResult, "失效日期"));
            }

            ocrResult.setData(data);
        }

        return ocrResult;
    }

    /**
     * 解析银行卡识别结果
     */
    private OcrResult parseBankCardResult(JSONObject result) {
        OcrResult ocrResult = new OcrResult();

        if (result.has("error_code")) {
            ocrResult.setSuccess(false);
            ocrResult.setErrorMsg(result.optString("error_msg", "识别失败"));
            return ocrResult;
        }

        ocrResult.setSuccess(true);
        JSONObject cardResult = result.optJSONObject("result");
        if (cardResult != null) {
            Map<String, String> data = new HashMap<>();
            data.put("卡号", cardResult.optString("bank_card_number", ""));
            data.put("有效期", cardResult.optString("valid_date", ""));
            data.put("银行", cardResult.optString("bank_name", ""));
            data.put("卡类型", cardResult.optString("bank_card_type", ""));
            ocrResult.setData(data);
        }

        return ocrResult;
    }

    /**
     * 解析驾驶证识别结果
     */
    private OcrResult parseDrivingLicenseResult(JSONObject result) {
        OcrResult ocrResult = new OcrResult();

        if (result.has("error_code")) {
            ocrResult.setSuccess(false);
            ocrResult.setErrorMsg(result.optString("error_msg", "识别失败"));
            return ocrResult;
        }

        ocrResult.setSuccess(true);
        JSONObject cardResult = result.optJSONObject("words_result");
        if (cardResult != null) {
            Map<String, String> data = new HashMap<>();
            data.put("证号", getWords(cardResult, "证号"));
            data.put("姓名", getWords(cardResult, "姓名"));
            data.put("性别", getWords(cardResult, "性别"));
            data.put("国籍", getWords(cardResult, "国籍"));
            data.put("住址", getWords(cardResult, "住址"));
            data.put("出生日期", getWords(cardResult, "出生日期"));
            data.put("初次领证日期", getWords(cardResult, "初次领证日期"));
            data.put("准驾车型", getWords(cardResult, "准驾车型"));
            data.put("有效期起始日期", getWords(cardResult, "有效期起始日期"));
            data.put("有效期截止日期", getWords(cardResult, "有效期截止日期"));
            ocrResult.setData(data);
        }

        return ocrResult;
    }

    /**
     * 解析行驶证识别结果
     */
    private OcrResult parseVehicleLicenseResult(JSONObject result) {
        OcrResult ocrResult = new OcrResult();

        if (result.has("error_code")) {
            ocrResult.setSuccess(false);
            ocrResult.setErrorMsg(result.optString("error_msg", "识别失败"));
            return ocrResult;
        }

        ocrResult.setSuccess(true);
        JSONObject cardResult = result.optJSONObject("words_result");
        if (cardResult != null) {
            Map<String, String> data = new HashMap<>();
            data.put("车牌号码", getWords(cardResult, "号牌号码"));
            data.put("车辆类型", getWords(cardResult, "车辆类型"));
            data.put("所有人", getWords(cardResult, "所有人"));
            data.put("品牌型号", getWords(cardResult, "品牌型号"));
            data.put("车辆识别代号", getWords(cardResult, "车辆识别代号"));
            data.put("发动机号码", getWords(cardResult, "发动机号码"));
            data.put("注册日期", getWords(cardResult, "注册日期"));
            data.put("发证日期", getWords(cardResult, "发证日期"));
            data.put("使用性质", getWords(cardResult, "使用性质"));
            ocrResult.setData(data);
        }

        return ocrResult;
    }

    /**
     * 解析医疗发票识别结果
     */
    private OcrResult parseMedicalInvoiceResult(JSONObject result) {
        OcrResult ocrResult = new OcrResult();

        if (result.has("error_code")) {
            ocrResult.setSuccess(false);
            ocrResult.setErrorMsg(result.optString("error_msg", "识别失败"));
            return ocrResult;
        }

        ocrResult.setSuccess(true);
        JSONObject data = result.optJSONObject("data");
        if (data != null) {
            // 转换为Map
            Map<String, Object> dataMap = new HashMap<>();
            for (String key : data.keySet()) {
                dataMap.put(key, data.get(key));
            }
            ocrResult.setData(dataMap);
        }

        return ocrResult;
    }

    /**
     * 从识别结果中获取指定字段的文字
     */
    private String getWords(JSONObject result, String key) {
        JSONObject field = result.optJSONObject(key);
        return field != null ? field.optString("words", "") : "";
    }
}