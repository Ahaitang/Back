package org.hospital.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.hospital.admin.entity.SessionLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会话审计日志 Mapper
 */
@Mapper
public interface SessionLogMapper extends BaseMapper<SessionLog> {}