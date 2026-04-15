package org.hospital.neuroimmune.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.hospital.neuroimmune.entity.Doctor;

@Mapper
public interface NeuroimmuneDoctorMapper extends BaseMapper<Doctor> {

    // 使用注解替代 XML
    @Select("SELECT id, name, title, department, hospital, phone, password, avatar, patient_count, create_time FROM doctor WHERE phone = #{phone}")
    Doctor selectByPhone(@Param("phone") String phone);
}