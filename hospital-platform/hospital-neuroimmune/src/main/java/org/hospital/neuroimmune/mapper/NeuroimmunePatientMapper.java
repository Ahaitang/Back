package org.hospital.neuroimmune.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.hospital.neuroimmune.entity.Patient;

@Mapper
public interface NeuroimmunePatientMapper extends BaseMapper<Patient> {

    @Select("SELECT id, name, gender, birth_date, phone, password, avatar, id_card, has_follow_up, is_real_auth, doctor_id, disease_type, create_time, update_time FROM patient WHERE phone = #{phone}")
    Patient selectByPhone(@Param("phone") String phone);

    @Select("SELECT COUNT(*) FROM patient WHERE doctor_id = #{doctorId}")
    Long selectCountByDoctorId(@Param("doctorId") Long doctorId);

    /**
     * 根据患者ID查询患者信息（包含医生姓名）
     */
    @Select("SELECT p.id, p.name, p.gender, p.birth_date, p.phone, p.password, p.avatar, p.id_card, " +
            "p.has_follow_up, p.is_real_auth, p.doctor_id, p.disease_type, p.create_time, p.update_time, " +
            "p.is_deleted, d.name AS doctor_name " +
            "FROM patient p " +
            "LEFT JOIN doctor d ON p.doctor_id = d.id " +
            "WHERE p.id = #{id}")
    Patient selectByIdWithDoctorName(@Param("id") Long id);

    /**
     * 根据手机号查询患者信息（包含医生姓名）
     */
    @Select("SELECT p.id, p.name, p.gender, p.birth_date, p.phone, p.password, p.avatar, p.id_card, " +
            "p.has_follow_up, p.is_real_auth, p.doctor_id, p.disease_type, p.create_time, p.update_time, " +
            "p.is_deleted, d.name AS doctor_name " +
            "FROM patient p " +
            "LEFT JOIN doctor d ON p.doctor_id = d.id " +
            "WHERE p.phone = #{phone}")
    Patient selectByPhoneWithDoctorName(@Param("phone") String phone);

    /**
     * 分页查询患者列表（包含医生姓名）- 基础查询
     */
    @Select("SELECT p.id, p.name, p.gender, p.birth_date, p.phone, p.avatar, p.id_card, " +
            "p.has_follow_up, p.is_real_auth, p.doctor_id, p.disease_type, p.create_time, p.update_time, " +
            "p.is_deleted, d.name AS doctor_name " +
            "FROM patient p " +
            "LEFT JOIN doctor d ON p.doctor_id = d.id " +
            "WHERE (p.is_deleted = 0 OR p.is_deleted IS NULL) " +
            "ORDER BY p.update_time DESC")
    List<Patient> selectListWithDoctorName();
}