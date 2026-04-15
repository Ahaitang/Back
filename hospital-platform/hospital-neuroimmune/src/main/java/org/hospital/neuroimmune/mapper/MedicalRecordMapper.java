package org.hospital.neuroimmune.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.hospital.neuroimmune.entity.MedicalRecord;

@Mapper
public interface MedicalRecordMapper extends BaseMapper<MedicalRecord> {
    // 完全使用 BaseMapper 的方法
}