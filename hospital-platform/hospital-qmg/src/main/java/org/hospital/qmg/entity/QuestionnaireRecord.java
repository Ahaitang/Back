package org.hospital.qmg.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 问卷结果实体类
 */
@Data
public class QuestionnaireRecord {
    /** 主键ID */
    private Integer id;
    /** 患者ID */
    private Integer patientId;
    /** 住院号（冗余字段，便于查询） */
    private String admissionNumber;
    /** 测评日期 */
    private LocalDate assessmentDate;
    /** 选择的选项（JSON格式） */
    private String selections;
    /** 各项得分（JSON格式） */
    private String itemScores;
    /** 总分 */
    private Integer totalScore;
    /** 分类得分（JSON格式） */
    private String categoryScores;
    /** 医生ID（创建记录的医生） */
    private Integer doctorId;
    /** 医生用户名（冗余字段） */
    private String doctorUsername;
    /** 最后修改人员（用户名） */
    private String modifiedBy;
    /** 用户自定义输入数据（JSON格式，如备注、说明等） */
    private String userInputData;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    // ========== 以下字段为联查字段，不持久化到数据库 ==========
    /** 患者姓名（联查字段） */
    private String patientName;
    /** 患者性别（联查字段） */
    private String patientGender;
    /** 患者电话（联查字段） */
    private String patientPhone;
    /** 医生姓名（联查字段） */
    private String doctorName;
    /** 医生工号（联查字段） */
    private String doctorEmployeeNumber;
}
