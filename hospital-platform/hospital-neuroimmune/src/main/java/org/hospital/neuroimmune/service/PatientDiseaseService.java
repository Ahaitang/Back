package org.hospital.neuroimmune.service;

import java.util.List;

public interface PatientDiseaseService {
    List<String> getDiseaseCodesByPatientId(Long patientId);
    void setDiseasesForPatient(Long patientId, List<String> diseaseCodes);
    List<Long> getPatientIdsByDiseaseCode(String diseaseCode);
}
