package org.hospital.neuroimmune.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.hospital.neuroimmune.dto.PageRequest;
import org.hospital.neuroimmune.dto.PageResult;
import org.hospital.neuroimmune.entity.FollowUp;
import org.hospital.neuroimmune.mapper.FollowUpMapper;
import org.hospital.neuroimmune.service.FollowUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FollowUpServiceImpl implements FollowUpService {

    @Autowired
    private FollowUpMapper followUpMapper;

    private static final Map<String, String> STATUS_TEXT_MAP = new HashMap<>();

    static {
        STATUS_TEXT_MAP.put("pending", "待随访");
        STATUS_TEXT_MAP.put("completed", "已完成");
        STATUS_TEXT_MAP.put("cancelled", "已取消");
    }

    @Override
    public PageResult<FollowUp> getList(PageRequest request) {
        List<FollowUp> list = followUpMapper.selectList(request);
        Long total = followUpMapper.selectCount(request);
        return new PageResult<>(list, total, request.getPageNum(), request.getPageSize());
    }

    @Override
    public FollowUp getById(Long id) {
        return followUpMapper.selectById(id);
    }

    @Override
    @CacheEvict(value = {"neuro-followup", "neuro-stats"}, allEntries = true)
    public void save(FollowUp followUp) {
        if (followUp.getStatus() == null) {
            followUp.setStatus("pending");
        }
        followUp.setStatusText(STATUS_TEXT_MAP.getOrDefault(followUp.getStatus(), followUp.getStatus()));

        if (followUp.getId() == null) {
            followUpMapper.insert(followUp);
        } else {
            followUpMapper.updateById(followUp);
        }
    }

    @Override
    public void updateStatus(Long id, String status) {
        LambdaUpdateWrapper<FollowUp> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(FollowUp::getId, id)
                .set(FollowUp::getStatus, status)
                .set(FollowUp::getStatusText, STATUS_TEXT_MAP.getOrDefault(status, status));
        followUpMapper.update(null, updateWrapper);
    }

    @Override
    public void delete(Long id) {
        followUpMapper.deleteById(id);
    }

    @Override
    @Cacheable(value = "neuro-stats", key = "'pending:total'")
    public Long getPendingCount() {
        return followUpMapper.selectPendingCount();
    }

    @Override
    @Cacheable(value = "neuro-stats", key = "'pending:doctor:' + #doctorId")
    public Long getPendingCountByDoctorId(Long doctorId) {
        return followUpMapper.selectPendingCountByDoctorId(doctorId);
    }

    @Override
    public PageResult<FollowUp> getListByDoctorId(Long doctorId, PageRequest request) {
        List<FollowUp> list = followUpMapper.selectListByDoctorId(doctorId, request);
        Long total = followUpMapper.selectCountByDoctorId(doctorId, request);
        return new PageResult<>(list, total, request.getPageNum(), request.getPageSize());
    }

    @Override
    public List<FollowUp> getPendingByDoctorId(Long doctorId) {
        return followUpMapper.selectPendingByDoctorId(doctorId);
    }

    @Override
    public Long getPendingCountByPatientId(Long patientId) {
        PageRequest request = new PageRequest();
        request.setPatientId(patientId);
        request.setStatus("pending");
        return followUpMapper.selectCount(request);
    }
}