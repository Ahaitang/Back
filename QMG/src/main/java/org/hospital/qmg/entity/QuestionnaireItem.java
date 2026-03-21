package org.hospital.qmg.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 问卷项目配置实体类
 */
@Data
public class QuestionnaireItem {
    /** 主键ID */
    private Integer id;
    /** 项目名称 */
    private String name;
    /** 项目键名（唯一标识） */
    private String key;
    /** 分类：eyes/bulbar/respiratory/limbs */
    private String category;
    /** 显示顺序 */
    private Integer displayOrder;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    /** 选项列表（关联字段，不持久化） */
    private List<QuestionnaireOption> options;
}
