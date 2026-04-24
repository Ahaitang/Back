package org.hospital.neuroimmune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hospital.common.model.PageResult;
import org.hospital.neuroimmune.entity.DiseaseEpisode;
import org.hospital.neuroimmune.mapper.DiseaseEpisodeMapper;
import org.hospital.neuroimmune.service.DiseaseEpisodeService;
import org.hospital.common.model.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiseaseEpisodeServiceImpl implements DiseaseEpisodeService {

    @Autowired
    private DiseaseEpisodeMapper diseaseEpisodeMapper;

    @Override
    public PageResult<DiseaseEpisode> getList(PageRequest request) {
        Page<DiseaseEpisode> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<DiseaseEpisode> wrapper = new LambdaQueryWrapper<>();
        // 只查询有效数据
        wrapper.eq(DiseaseEpisode::getIsDeleted, 0).or().isNull(DiseaseEpisode::getIsDeleted);
        if (request.getPatientId() != null) {
            wrapper.eq(DiseaseEpisode::getPatientId, request.getPatientId());
        }
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(DiseaseEpisode::getChiefComplaint, request.getKeyword())
                    .or().like(DiseaseEpisode::getDiagnosis, request.getKeyword()));
        }
        wrapper.orderByDesc(DiseaseEpisode::getEpisodeDate).orderByDesc(DiseaseEpisode::getCreateTime);

        Page<DiseaseEpisode> result = diseaseEpisodeMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public DiseaseEpisode getById(Long id) {
        return diseaseEpisodeMapper.selectById(id);
    }

    @Override
    public List<DiseaseEpisode> getByPatientId(Long patientId) {
        LambdaQueryWrapper<DiseaseEpisode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DiseaseEpisode::getPatientId, patientId);
        wrapper.eq(DiseaseEpisode::getIsDeleted, 0).or().isNull(DiseaseEpisode::getIsDeleted);
        wrapper.orderByDesc(DiseaseEpisode::getEpisodeDate).orderByAsc(DiseaseEpisode::getEpisodeNumber);
        return diseaseEpisodeMapper.selectList(wrapper);
    }

    @Override
    public Integer countByPatientId(Long patientId) {
        LambdaQueryWrapper<DiseaseEpisode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DiseaseEpisode::getPatientId, patientId);
        wrapper.eq(DiseaseEpisode::getIsDeleted, 0).or().isNull(DiseaseEpisode::getIsDeleted);
        Long count = diseaseEpisodeMapper.selectCount(wrapper);
        return count != null ? count.intValue() : 0;
    }

    @Override
    public void save(DiseaseEpisode episode) {
        if (episode.getId() == null) {
            diseaseEpisodeMapper.insert(episode);
        } else {
            diseaseEpisodeMapper.updateById(episode);
        }
    }

    @Override
    public void delete(Long id) {
        // 逻辑删除
        LambdaUpdateWrapper<DiseaseEpisode> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DiseaseEpisode::getId, id).set(DiseaseEpisode::getIsDeleted, 1);
        diseaseEpisodeMapper.update(null, updateWrapper);
    }

    @Override
    public void deleteByPatientId(Long patientId) {
        // 逻辑删除患者所有发作记录
        LambdaUpdateWrapper<DiseaseEpisode> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DiseaseEpisode::getPatientId, patientId)
                     .eq(DiseaseEpisode::getIsDeleted, 0).or().isNull(DiseaseEpisode::getIsDeleted)
                     .set(DiseaseEpisode::getIsDeleted, 1);
        diseaseEpisodeMapper.update(null, updateWrapper);
    }
}