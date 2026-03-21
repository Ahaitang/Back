package org.hospital.neuroimmune.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.hospital.neuroimmune.entity.FollowUp;
import org.hospital.neuroimmune.dto.PageRequest;
import java.util.List;

@Mapper
public interface FollowUpMapper extends BaseMapper<FollowUp> {

    List<FollowUp> selectList(PageRequest request);

    Long selectCount(PageRequest request);

    Long selectPendingCount();

    Long selectPendingCountByDoctorId(Long doctorId);

    List<FollowUp> selectListByDoctorId(@Param("doctorId") Long doctorId, @Param("request") PageRequest request);

    Long selectCountByDoctorId(@Param("doctorId") Long doctorId, @Param("request") PageRequest request);

    List<FollowUp> selectPendingByDoctorId(Long doctorId);
}