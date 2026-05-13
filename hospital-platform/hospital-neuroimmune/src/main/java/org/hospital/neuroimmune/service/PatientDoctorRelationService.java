package org.hospital.neuroimmune.service;

import org.hospital.neuroimmune.entity.PatientDoctorRelation;
import java.util.List;
import java.util.Map;

public interface PatientDoctorRelationService {

    /**
     * 绑定患者和医生
     */
    boolean bindDoctor(Long patientId, Long doctorId, String bindMethod, String remark);

    /**
     * 解除绑定
     */
    boolean unbind(Long id);

    /**
     * 解除患者当前绑定
     */
    boolean unbindPatient(Long patientId);

    /**
     * 获取患者当前绑定的医生
     */
    PatientDoctorRelation getActiveDoctor(Long patientId);

    /**
     * 获取患者最新的绑定关系（包括审核状态）
     * 用于前端判断是否需要弹出选择弹窗
     */
    PatientDoctorRelation getLatestRelation(Long patientId);

    /**
     * 批量获取患者的主治医生姓名
     * @param patientIds 患者ID列表
     * @return Map<patientId, doctorName>
     */
    Map<Long, String> batchGetDoctorNames(List<Long> patientIds);

    /**
     * 批量获取患者的主治医生信息
     * @param patientIds 患者ID列表
     * @return Map<patientId, PatientDoctorRelation>
     */
    Map<Long, PatientDoctorRelation> batchGetActiveDoctorInfo(List<Long> patientIds);

    /**
     * 获取医生的所有患者
     */
    List<PatientDoctorRelation> getPatientsByDoctor(Long doctorId);

    /**
     * 获取医生的所有生效中患者
     */
    List<PatientDoctorRelation> getActivePatientsByDoctor(Long doctorId);

    /**
     * 获取患者的绑定历史
     */
    List<PatientDoctorRelation> getBindHistory(Long patientId);

    /**
     * 查询所有绑定关系
     */
    List<PatientDoctorRelation> getList(String patientName, String doctorName, String status);

    /**
     * 统计医生的患者数量
     */
    Long countPatientsByDoctor(Long doctorId);

    /**
     * 根据ID查询
     */
    PatientDoctorRelation getById(Long id);

    /**
     * 根据患者和医生查询绑定关系
     */
    PatientDoctorRelation getByPatientAndDoctor(Long patientId, Long doctorId);

    /**
     * 解除患者的所有绑定关系（患者删除时调用）
     */
    void unbindAllByPatientId(Long patientId);

    /**
     * 解除医生的所有绑定关系（医生删除时调用）
     */
    void unbindAllByDoctorId(Long doctorId);

    /**
     * 批量统计医生的患者数量
     */
    Map<Long, Long> countByDoctorIds(List<Long> doctorIds);

    /**
     * 创建待确认的绑定关系
     */
    void createPendingRelation(PatientDoctorRelation relation);

    /**
     * 获取医生的待确认患者列表
     */
    List<PatientDoctorRelation> getPendingRelationsByDoctor(Long doctorId);

    /**
     * 获取医生的已确认患者列表
     */
    List<PatientDoctorRelation> getConfirmedRelationsByDoctor(Long doctorId);

    /**
     * 获取医生的已拒绝患者列表
     */
    List<PatientDoctorRelation> getRejectedRelationsByDoctor(Long doctorId);

    /**
     * 确认绑定关系
     */
    boolean confirmRelation(Long relationId);

    /**
     * 拒绝绑定关系
     */
    boolean rejectRelation(Long relationId);

    /**
     * 按绑定状态查询患者ID列表
     */
    List<Long> getPatientIdsByBindStatus(Integer bindStatus);

    /**
     * 查询有绑定关系记录的所有患者ID
     */
    List<Long> getAllPatientIdsWithRelation();
}