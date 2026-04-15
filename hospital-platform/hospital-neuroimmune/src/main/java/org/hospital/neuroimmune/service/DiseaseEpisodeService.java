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
}