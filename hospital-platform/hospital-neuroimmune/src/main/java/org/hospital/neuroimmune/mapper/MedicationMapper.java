package org.hospital.neuroimmune.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.hospital.neuroimmune.entity.Medication;
import java.util.List;

@Mapper
public interface MedicationMapper extends BaseMapper<Medication> {

    @Select("SELECT CAST(COUNT(*) AS UNSIGNED) FROM medication WHERE doctor_id = #{doctorId}")
    Long selectCountByDoctorId(@Param("doctorId") Long doctorId);

    @Select("SELECT m.id, m.patient_id, m.doctor_id, m.medication_name, m.date, m.dosage_value, m.dosage_unit, m.frequency, m.route, m.duration, m.end_date, m.notes, m.status, m.create_time, p.name AS patient_name, d.name AS doctor_name FROM medication m LEFT JOIN patient p ON m.patient_id = p.id LEFT JOIN doctor d ON m.doctor_id = d.id WHERE (m.is_deleted = 0 OR m.is_deleted IS NULL) ORDER BY m.date DESC")
    List<Medication> selectAllMedications();
}