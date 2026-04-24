package org.hospital.neuroimmune.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 通用字典实体
 * 支持多种字典类型：department(科室)、title(职称)、recordType(病历类型)、
 * followUpType(随访类型)、gender(性别)、status(状态)等
 */
@Data
@TableName("dict_common")
public class CommonDict {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String dictType;     // 字典类型：department/title/recordType/followUpType/gender/status
    private String code;         // 编码（可选，用于国际化或程序识别）
    private String name;         // 名称（显示值）
    private String description;  // 描述
    private Integer sortOrder;   // 排序顺序
    private Integer isActive;    // 是否启用：1-启用，0-禁用

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}