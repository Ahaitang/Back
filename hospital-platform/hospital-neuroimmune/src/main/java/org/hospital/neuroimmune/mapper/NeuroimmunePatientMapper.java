package org.hospital.neuroimmune.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.hospital.neuroimmune.entity.Patient;

@Mapper
public interface NeuroimmunePatientMapper extends BaseMapper<Patient> {

    @Select("SELECT id, name, gender, age, phone, password, avatar, id_card, has_follow_up, is_real_auth, doctor_id, doctor_name, disease_type, create_time, update_time FROM patient WHERE phone = #{phone}")
    Patient selectByPhone(@Param("phone") String phone);

    @Select("SELECT COUNT(*) FROM patient WHERE doctor_id = #{doctorId}")
    Long selectCountByDoctorId(@Param("doctorId") Long doctorId);
}