package org.hospital.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.hospital.common.entity.SessionLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会话审计日志 Mapper
 */
@Mapper
public interface SessionLogMapper extends BaseMapper<SessionLog> {}