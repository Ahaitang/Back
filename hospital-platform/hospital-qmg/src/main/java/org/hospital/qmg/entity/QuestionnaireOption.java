package org.hospital.qmg.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 问卷选项配置实体类
 */
@Data
public class QuestionnaireOption {
    /** 主键ID */
    private Integer id;
    /** 项目键名 */
    private String itemKey;
    /** 选项标签 */
    private String label;
    /** 选项值 */
    private String value;
    /** 得分 */
    private Integer score;
    /** 显示顺序 */
    private Integer displayOrder;
    /** 是否允许用户输入自定义数据（0=不允许，1=允许） */
    private Boolean allowCustomInput;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
