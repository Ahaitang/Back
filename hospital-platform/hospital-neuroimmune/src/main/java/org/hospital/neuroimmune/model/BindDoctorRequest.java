package org.hospital.neuroimmune.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 患者绑定医生请求模型
 */
@Data
public class BindDoctorRequest {

    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    @NotNull(message = "医生ID不能为空")
    private Long doctorId;

    private String bindMethod = "patient";

    private String remark;
}