package org.hospital.admin.model;

import lombok.Data;

/**
 * 日志文件信息
 */
@Data
public class LogFileInfo {
    private String name;
    private Long size;
    private String lastModified;
}
