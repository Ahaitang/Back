package org.hospital.neuroimmune.service;

import org.hospital.common.model.PageResult;
import org.hospital.neuroimmune.entity.DiseaseEpisode;
import org.hospital.common.model.PageRequest;
import java.util.List;

public interface DiseaseEpisodeService {

    PageResult<DiseaseEpisode> getList(PageRequest request);

    DiseaseEpisode getById(Long id);

    List<DiseaseEpisode> getByPatientId(Long patientId);

    void save(DiseaseEpisode episode);

    void delete(Long id);

    Integer countByPatientId(Long patientId);

    Integer countByDoctorId(Long doctorId);

    /**
     * 逻辑删除患者的所有发作记录（患者删除时调用）
     */
    void deleteByPatientId(Long patientId);
}