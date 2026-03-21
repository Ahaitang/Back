package org.hospital.neuroimmune.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.hospital.neuroimmune.entity.MedicalRecord;
import org.hospital.neuroimmune.dto.PageRequest;
import java.util.List;

@Mapper
public interface MedicalRecordMapper extends BaseMapper<MedicalRecord> {

    List<MedicalRecord> selectList(PageRequest request);

    Long selectCount(PageRequest request);

    List<MedicalRecord> selectListByDoctorId(@Param("doctorId") Long doctorId, @Param("request") PageRequest request);

    Long selectCountByDoctorId(@Param("doctorId") Long doctorId, @Param("request") PageRequest request);
}