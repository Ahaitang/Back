package org.hospital.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.hospital.common.entity.BlackList;
import org.apache.ibatis.annotations.Mapper;

/**
 * 黑名单 Mapper
 */
@Mapper
public interface BlackListMapper extends BaseMapper<BlackList> {}