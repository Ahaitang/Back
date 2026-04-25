package org.hospital.neuroimmune.controller;

import org.hospital.common.model.Result;
import org.hospital.common.model.ImportResult;
import org.hospital.neuroimmune.service.ImportService;
import org.hospital.common.util.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 导入控制器
 * 已重构：使用 ImportService 将导入逻辑下沉到 Service 层
 */
@RestController
@RequestMapping("/api/v1/neuroimmune/import")
@CrossOrigin
public class ImportController {

    @Autowired
    private ImportService importService;

    /**
     * 下载医生导入模板
     */
    @GetMapping("/doctor/template")
    public ResponseEntity<byte[]> downloadDoctorTemplate() throws IOException {
        byte[] data = ExcelUtil.generateDoctorTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=doctor_template.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    /**
     * 下载患者导入模板
     */
    @GetMapping("/patient/template")
    public ResponseEntity<byte[]> downloadPatientTemplate() throws IOException {
        byte[] data = ExcelUtil.generatePatientTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=patient_template.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    /**
     * 导入医生
     */
    @PostMapping("/doctor")
    public Result<ImportResult> importDoctors(@RequestParam("file") MultipartFile file) {
        try {
            ImportResult result = importService.importDoctors(file);
            return Result.success(result);
        } catch (IOException e) {
            return Result.error("读取文件失败: " + e.getMessage());
        }
    }

    /**
     * 导入患者
     */
    @PostMapping("/patient")
    public Result<ImportResult> importPatients(@RequestParam("file") MultipartFile file) {
        try {
            ImportResult result = importService.importPatients(file);
            return Result.success(result);
        } catch (IOException e) {
            return Result.error("读取文件失败: " + e.getMessage());
        }
    }
}