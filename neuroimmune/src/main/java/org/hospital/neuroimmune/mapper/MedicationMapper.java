package org.hospital.neuroimmune.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.hospital.neuroimmune.entity.Medication;
import org.hospital.neuroimmune.dto.PageRequest;
import java.util.List;

@Mapper
public interface MedicationMapper extends BaseMapper<Medication> {

    List<Medication> selectList(PageRequest request);

    Long selectCount(PageRequest request);

    Long selectCountByDoctorId(Long doctorId);

    List<Medication> selectListByDoctorId(@Param("doctorId") Long doctorId, @Param("request") PageRequest request);
}