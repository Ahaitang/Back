package org.hospital.neuroimmune.service.impl;

import org.hospital.neuroimmune.dto.PageResult;
import org.hospital.neuroimmune.entity.DiseaseEpisode;
import org.hospital.neuroimmune.mapper.DiseaseEpisodeMapper;
import org.hospital.neuroimmune.service.DiseaseEpisodeService;
import org.hospital.neuroimmune.dto.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiseaseEpisodeServiceImpl implements DiseaseEpisodeService {

    @Autowired
    private DiseaseEpisodeMapper diseaseEpisodeMapper;

    @Override
    public PageResult<DiseaseEpisode> getList(PageRequest request) {
        List<DiseaseEpisode> list = diseaseEpisodeMapper.selectList(request);
        Long total = diseaseEpisodeMapper.selectCount(request);
        return new PageResult<>(list, total, request.getPageNum(), request.getPageSize());
    }

    @Override
    public DiseaseEpisode getById(Long id) {
        return diseaseEpisodeMapper.selectById(id);
    }

    @Override
    public List<DiseaseEpisode> getByPatientId(Long patientId) {
        return diseaseEpisodeMapper.selectByPatientId(patientId);
    }

    @Override
    public void save(DiseaseEpisode episode) {
        if (episode.getId() == null) {
            // 新增时自动计算发作次数
            if (episode.getEpisodeNumber() == null && episode.getPatientId() != null) {
                Integer count = diseaseEpisodeMapper.countByPatientId(episode.getPatientId());
                episode.setEpisodeNumber(count == null ? 1 : count + 1);
            }
            diseaseEpisodeMapper.insert(episode);
        } else {
            diseaseEpisodeMapper.updateById(episode);
        }
    }

    @Override
    public void delete(Long id) {
        diseaseEpisodeMapper.deleteById(id);
    }

    @Override
    public Integer countByPatientId(Long patientId) {
        Integer count = diseaseEpisodeMapper.countByPatientId(patientId);
        return count == null ? 0 : count;
    }
}