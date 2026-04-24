package org.hospital.neuroimmune.entity;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("doctor_role")
public class DoctorRole {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long doctorId;
    private String roleCode;
    private Integer isActive;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    private Integer isDeleted;     // 0-有效, 1-无效
}