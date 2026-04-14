package org.hospital.neuroimmune.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.hospital.neuroimmune.entity.Patient;
import org.hospital.neuroimmune.dto.PageRequest;
import java.util.List;

@Mapper
public interface PatientMapper extends BaseMapper<Patient> {

    List<Patient> selectList(PageRequest request);

    Long selectCount(PageRequest request);

    Patient selectByPhone(String phone);

    Long selectCountByDoctorId(Long doctorId);
}