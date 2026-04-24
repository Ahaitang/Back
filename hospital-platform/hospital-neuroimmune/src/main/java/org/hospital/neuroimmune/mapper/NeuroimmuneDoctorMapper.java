package org.hospital.neuroimmune.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.hospital.neuroimmune.entity.Doctor;
import java.util.List;

@Mapper
public interface NeuroimmuneDoctorMapper extends BaseMapper<Doctor> {

    @Select("SELECT id, name, title, department, hospital, phone, password, avatar, create_time, is_deleted, level FROM doctor WHERE phone = #{phone}")
    Doctor selectByPhone(@Param("phone") String phone);

    @Select("SELECT d.id, d.name, d.title, d.department, d.hospital, d.phone, d.password, d.avatar, d.create_time, d.is_deleted, d.level, GROUP_CONCAT(dr.role_code) as roles " +
            "FROM doctor d " +
            "INNER JOIN doctor_role dr ON d.id = dr.doctor_id AND dr.is_active = 1 AND dr.role_code = 'DOCTOR' " +
            "WHERE d.is_deleted = 0 " +
            "GROUP BY d.id " +
            "ORDER BY d.create_time DESC")
    List<Doctor> selectDoctorsWithRoles();

    @Select("SELECT d.id, d.name, d.title, d.department, d.hospital, d.phone, d.password, d.avatar, d.create_time, d.is_deleted, d.level, GROUP_CONCAT(dr.role_code) as roles " +
            "FROM doctor d " +
            "INNER JOIN doctor_role dr ON d.id = dr.doctor_id AND dr.is_active = 1 AND dr.role_code = 'ADMIN' " +
            "WHERE d.is_deleted = 0 " +
            "GROUP BY d.id " +
            "ORDER BY d.level ASC, d.create_time DESC")
    List<Doctor> selectAdminsWithRoles();
}