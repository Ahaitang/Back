package org.hospital.neuroimmune.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.hospital.neuroimmune.entity.PatientDisease;
import java.util.List;

public interface PatientDiseaseMapper extends BaseMapper<PatientDisease> {
    @Select("SELECT disease_code FROM patient_disease WHERE patient_id = #{patientId}")
    List<String> findDiseaseCodesByPatientId(@Param("patientId") Long patientId);

    @Select("SELECT patient_id FROM patient_disease WHERE disease_code = #{diseaseCode}")
    List<Long> findPatientIdsByDiseaseCode(@Param("diseaseCode") String diseaseCode);

    @Select("SELECT patient_id FROM patient_disease WHERE disease_code IN (${diseaseCodes})")
    List<Long> findPatientIdsByDiseaseCodes(@Param("diseaseCodes") String diseaseCodes);
}
