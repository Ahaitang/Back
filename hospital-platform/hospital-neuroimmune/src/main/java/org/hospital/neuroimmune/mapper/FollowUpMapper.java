package org.hospital.neuroimmune.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.hospital.neuroimmune.entity.FollowUp;
import java.util.List;

@Mapper
public interface FollowUpMapper extends BaseMapper<FollowUp> {

    // 保留特殊的统计方法（这些不适合用条件构造器）
    @Select("SELECT COUNT(*) FROM follow_up WHERE status = 'pending'")
    Long selectPendingCount();

    @Select("SELECT COUNT(*) FROM follow_up WHERE doctor_id = #{doctorId} AND status = 'pending'")
    Long selectPendingCountByDoctorId(@Param("doctorId") Long doctorId);

    @Select("SELECT * FROM follow_up WHERE doctor_id = #{doctorId} AND status = 'pending' ORDER BY date ASC")
    List<FollowUp> selectPendingByDoctorId(@Param("doctorId") Long doctorId);
}