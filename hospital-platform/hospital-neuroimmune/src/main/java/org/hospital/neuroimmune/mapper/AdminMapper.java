package org.hospital.neuroimmune.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.hospital.neuroimmune.entity.Admin;

@Mapper
public interface AdminMapper extends BaseMapper<Admin> {

    @Select("SELECT id, username, password, name, level, create_time FROM admin WHERE username = #{username}")
    Admin selectByUsername(String username);
}