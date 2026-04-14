package org.hospital.neuroimmune.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.hospital.neuroimmune.entity.Admin;

@Mapper
public interface AdminMapper extends BaseMapper<Admin> {

    Admin selectByUsername(String username);
}