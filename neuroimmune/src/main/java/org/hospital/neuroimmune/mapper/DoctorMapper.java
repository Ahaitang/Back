package org.hospital.neuroimmune.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.hospital.neuroimmune.entity.Doctor;
import org.hospital.neuroimmune.dto.PageRequest;
import java.util.List;

@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {

    List<Doctor> selectList(PageRequest request);

    Long selectCount(PageRequest request);

    Doctor selectByPhone(String phone);
}