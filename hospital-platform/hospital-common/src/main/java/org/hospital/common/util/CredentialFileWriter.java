package org.hospital.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 凭证文件写入工具
 * 各模块启动时若自动生成了默认密码，统一追加写入 user.txt
 */
public class CredentialFileWriter {

    private static final Logger logger = LoggerFactory.getLogger(CredentialFileWriter.class);
    private static final Path FILE_PATH = Paths.get("user.txt");
    private static boolean headerWritten = false;

    private CredentialFileWriter() {
    }

    /**
     * 追加一条凭证信息到 user.txt
     *
     * @param module   模块名称（如 "超级管理员"、"神经免疫系统"、"QMG 评分系统"）
     * @param username 用户名
     * @param password 明文密码
     */
    public static synchronized void writeCredential(String module, String username, String password) {
        try {
            ensureHeader();
            StringBuilder content = new StringBuilder();
            content.append("== ").append(module).append(" ==\n");
            content.append("用户名: ").append(username).append("\n");
            content.append("密码: ").append(password).append("\n\n");
            Files.writeString(FILE_PATH, content.toString(), StandardOpenOption.APPEND);
            logger.info("[{}] 默认凭证已写入 {}", module, FILE_PATH.toAbsolutePath());
        } catch (IOException e) {
            logger.error("写入 user.txt 失败: {}", e.getMessage());
        }
    }

    private static void ensureHeader() throws IOException {
        if (!headerWritten) {
            String header = "# 系统默认账号信息（自动生成于 "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    + "）\n"
                    + "# 请妥善保管，建议设置环境变量后删除此文件\n\n";
            Files.writeString(FILE_PATH, header);
            headerWritten = true;
        }
    }
}
