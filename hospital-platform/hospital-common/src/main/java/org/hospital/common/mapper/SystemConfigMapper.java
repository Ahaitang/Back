package org.hospital.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.hospital.common.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统配置 Mapper
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {}