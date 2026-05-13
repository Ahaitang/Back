package org.hospital.neuroimmune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hospital.common.model.PageResult;
import org.hospital.neuroimmune.entity.DiseaseEpisode;
import org.hospital.neuroimmune.entity.Patient;
import org.hospital.neuroimmune.mapper.DiseaseEpisodeMapper;
import org.hospital.neuroimmune.mapper.NeuroimmunePatientMapper;
import org.hospital.neuroimmune.service.DiseaseEpisodeService;
import org.hospital.common.model.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DiseaseEpisodeServiceImpl implements DiseaseEpisodeService {

    @Autowired
    private DiseaseEpisodeMapper diseaseEpisodeMapper;

    @Autowired
    private NeuroimmunePatientMapper patientMapper;

    @Override
    public PageResult<DiseaseEpisode> getList(PageRequest request) {
        Page<DiseaseEpisode> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<DiseaseEpisode> wrapper = new LambdaQueryWrapper<>();
        // 只查询有效数据（未删除的记录）
        wrapper.and(w -> w.eq(DiseaseEpisode::getIsDeleted, 0).or().isNull(DiseaseEpisode::getIsDeleted));
        if (request.getPatientId() != null) {
            wrapper.eq(DiseaseEpisode::getPatientId, request.getPatientId());
        }
        if (request.getDoctorId() != null) {
            wrapper.apply("patient_id IN (SELECT patient_id FROM patient_doctor_relation WHERE doctor_id = {0} AND status = 1 AND bind_status = 1)", request.getDoctorId());
        }
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(DiseaseEpisode::getChiefComplaint, request.getKeyword())
                    .or().like(DiseaseEpisode::getDiagnosis, request.getKeyword()));
        }
        wrapper.orderByDesc(DiseaseEpisode::getEpisodeDate).orderByDesc(DiseaseEpisode::getCreateTime);

        Page<DiseaseEpisode> result = diseaseEpisodeMapper.selectPage(page, wrapper);
        enrichWithNames(result.getRecords());
        return new PageResult<>(result.getRecords(), result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public DiseaseEpisode getById(Long id) {
        DiseaseEpisode episode = diseaseEpisodeMapper.selectById(id);
        if (episode != null) enrichSingle(episode);
        return episode;
    }

    @Override
    public List<DiseaseEpisode> getByPatientId(Long patientId) {
        LambdaQueryWrapper<DiseaseEpisode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DiseaseEpisode::getPatientId, patientId);
        wrapper.eq(DiseaseEpisode::getIsDeleted, 0).or().isNull(DiseaseEpisode::getIsDeleted);
        wrapper.orderByDesc(DiseaseEpisode::getEpisodeDate).orderByAsc(DiseaseEpisode::getEpisodeNumber);
        List<DiseaseEpisode> list = diseaseEpisodeMapper.selectList(wrapper);
        enrichWithNames(list);
        return list;
    }

    @Override
    public Integer countByPatientId(Long patientId) {
        LambdaQueryWrapper<DiseaseEpisode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DiseaseEpisode::getPatientId, patientId);
        wrapper.eq(DiseaseEpisode::getIsDeleted, 0).or().isNull(DiseaseEpisode::getIsDeleted);
        Object count = diseaseEpisodeMapper.selectCount(wrapper);
        return count != null ? Integer.valueOf(count.toString()) : 0;
    }

    @Override
    public Integer countByDoctorId(Long doctorId) {
        // 通过医生ID统计其所有患者的发作记录数量
        return diseaseEpisodeMapper.countByDoctorId(doctorId);
    }

    @Override
    public void save(DiseaseEpisode episode) {
        if (episode.getId() == null) {
            // 新增时自动计算发作次数
            if (episode.getEpisodeNumber() == null && episode.getPatientId() != null) {
                Integer count = countByPatientId(episode.getPatientId());
                episode.setEpisodeNumber(count + 1);
            }
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

    private void enrichWithNames(List<DiseaseEpisode> episodes) {
        if (episodes == null || episodes.isEmpty()) return;

        List<Long> patientIds = episodes.stream()
                .map(DiseaseEpisode::getPatientId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Patient> patientMap = patientIds.isEmpty() ? Map.of() :
                patientMapper.selectBatchIds(patientIds).stream()
                        .collect(Collectors.toMap(Patient::getId, p -> p, (a, b) -> a));

        episodes.forEach(e -> {
            Patient p = patientMap.get(e.getPatientId());
            if (p != null) e.setPatientName(p.getName());
        });
    }

    private void enrichSingle(DiseaseEpisode episode) {
        if (episode.getPatientId() != null) {
            Patient p = patientMapper.selectById(episode.getPatientId());
            if (p != null) episode.setPatientName(p.getName());
        }
    }
}
