package org.hospital.neuroimmune.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.hospital.neuroimmune.entity.CommonDict;

import java.util.List;

@Mapper
public interface CommonDictMapper extends BaseMapper<CommonDict> {

    @Select("SELECT id, dict_type, code, name, description, sort_order, is_active, create_time, update_time " +
            "FROM dict_common WHERE dict_type = #{dictType} AND is_active = 1 ORDER BY sort_order ASC, id ASC")
    List<CommonDict> selectByType(@Param("dictType") String dictType);
}