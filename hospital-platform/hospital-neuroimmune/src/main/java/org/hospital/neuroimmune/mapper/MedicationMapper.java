package org.hospital.neuroimmune.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.hospital.neuroimmune.entity.Medication;
import java.util.List;

@Mapper
public interface MedicationMapper extends BaseMapper<Medication> {

    @Select("SELECT COUNT(*) FROM medication WHERE doctor_id = #{doctorId}")
    Long selectCountByDoctorId(@Param("doctorId") Long doctorId);

    @Select("SELECT id, patient_id, patient_name, doctor_id, doctor_name, medication_name, date, dosage, unit, frequency, route, duration, end_date, notes, create_time FROM medication ORDER BY date DESC")
    List<Medication> selectAllMedications();
}