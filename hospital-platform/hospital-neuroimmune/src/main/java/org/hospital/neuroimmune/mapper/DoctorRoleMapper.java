package org.hospital.neuroimmune.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.hospital.neuroimmune.entity.DoctorRole;
import java.util.List;

public interface DoctorRoleMapper extends BaseMapper<DoctorRole> {
    @Select("SELECT role_code FROM doctor_role WHERE doctor_id = #{doctorId} AND is_active = 1")
    List<String> findRoleCodesByDoctorId(@Param("doctorId") Long doctorId);
}