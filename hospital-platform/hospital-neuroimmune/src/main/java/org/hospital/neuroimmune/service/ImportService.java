package org.hospital.neuroimmune.service;

import org.hospital.common.model.ImportResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 导入服务
 * 处理医生和患者导入逻辑
 */
public interface ImportService {

    /**
     * 导入医生
     * @param file Excel文件
     * @return 导入结果
     */
    ImportResult importDoctors(MultipartFile file) throws IOException;

    /**
     * 导入患者
     * @param file Excel文件
     * @return 导入结果
     */
    ImportResult importPatients(MultipartFile file) throws IOException;
}