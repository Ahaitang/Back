package org.hospital.neuroimmune.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.hospital.neuroimmune.entity.PatientDoctorRelation;
import java.util.List;

@Mapper
public interface PatientDoctorRelationMapper extends BaseMapper<PatientDoctorRelation> {

    /**
     * 查询患者的所有绑定关系
     */
    List<PatientDoctorRelation> selectByPatientId(Long patientId);

    /**
     * 查询患者的当前生效绑定
     */
    PatientDoctorRelation selectActiveByPatientId(Long patientId);

    /**
     * 查询医生的所有患者绑定
     */
    List<PatientDoctorRelation> selectByDoctorId(Long doctorId);

    /**
     * 查询医生的生效中患者绑定
     */
    List<PatientDoctorRelation> selectActiveByDoctorId(Long doctorId);

    /**
     * 查询特定患者-医生绑定
     */
    PatientDoctorRelation selectByPatientAndDoctor(@Param("patientId") Long patientId, @Param("doctorId") Long doctorId);

    /**
     * 查询所有绑定关系（分页）
     */
    List<PatientDoctorRelation> selectList(@Param("patientName") String patientName, @Param("doctorName") String doctorName, @Param("status") String status);

    /**
     * 统计医生的患者数量
     */
    Long countByDoctorId(Long doctorId);

    /**
     * 解除绑定（更新状态为inactive）
     */
    int unbind(Long id);
}