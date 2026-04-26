package org.hospital.neuroimmune.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.hospital.neuroimmune.entity.Patient;

@Mapper
public interface NeuroimmunePatientMapper extends BaseMapper<Patient> {

    @Select("SELECT id, name, gender, birth_date, phone, password, avatar, id_card, is_real_auth, create_time, update_time, is_deleted FROM patient WHERE phone = #{phone}")
    Patient selectByPhone(@Param("phone") String phone);

    /**
     * 更新患者状态
     */
    @Update("UPDATE patient SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}