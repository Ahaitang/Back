package org.hospital.neuroimmune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hospital.common.model.PageRequest;
import org.hospital.common.model.PageResult;
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
        Page<FollowUp> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<FollowUp> wrapper = buildQueryWrapper(request);
        wrapper.orderByDesc(FollowUp::getDate).orderByDesc(FollowUp::getCreateTime);

        Page<FollowUp> result = followUpMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public PageResult<FollowUp> getListByDoctorId(Long doctorId, PageRequest request) {
        Page<FollowUp> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<FollowUp> wrapper = buildQueryWrapper(request);
        wrapper.eq(FollowUp::getDoctorId, doctorId);
        wrapper.orderByDesc(FollowUp::getDate).orderByDesc(FollowUp::getCreateTime);

        Page<FollowUp> result = followUpMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    private LambdaQueryWrapper<FollowUp> buildQueryWrapper(PageRequest request) {
        LambdaQueryWrapper<FollowUp> wrapper = new LambdaQueryWrapper<>();

        if (request.getPatientId() != null) {
            wrapper.eq(FollowUp::getPatientId, request.getPatientId());
        }
        if (request.getDoctorId() != null) {
            wrapper.eq(FollowUp::getDoctorId, request.getDoctorId());
        }
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(FollowUp::getPatientName, request.getKeyword())
                    .or().like(FollowUp::getDoctorName, request.getKeyword())
                    .or().like(FollowUp::getProject, request.getKeyword()));
        }
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            wrapper.eq(FollowUp::getStatus, request.getStatus());
        }
        if (request.getType() != null && !request.getType().isEmpty()) {
            wrapper.eq(FollowUp::getType, request.getType());
        }
        if (request.getStartDate() != null && !request.getStartDate().isEmpty()) {
            wrapper.ge(FollowUp::getDate, request.getStartDate());
        }
        if (request.getEndDate() != null && !request.getEndDate().isEmpty()) {
            wrapper.le(FollowUp::getDate, request.getEndDate());
        }

        return wrapper;
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
    @CacheEvict(value = {"neuro-followup", "neuro-stats"}, allEntries = true)
    public void delete(Long id) {
        followUpMapper.deleteById(id);
    }

    @Override
    @Cacheable(value = "neuro-stats", key = "'followup:pending'")
    public Long getPendingCount() {
        return followUpMapper.selectPendingCount();
    }

    @Override
    @Cacheable(value = "neuro-stats", key = "'followup:pending:doctor:' + #doctorId")
    public Long getPendingCountByDoctorId(Long doctorId) {
        return followUpMapper.selectPendingCountByDoctorId(doctorId);
    }

    @Override
    public List<FollowUp> getPendingByDoctorId(Long doctorId) {
        return followUpMapper.selectPendingByDoctorId(doctorId);
    }

    @Override
    public Long getPendingCountByPatientId(Long patientId) {
        LambdaQueryWrapper<FollowUp> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowUp::getPatientId, patientId)
               .eq(FollowUp::getStatus, "pending");
        return followUpMapper.selectCount(wrapper);
    }

    @Override
    @CacheEvict(value = {"neuro-followup", "neuro-stats"}, allEntries = true)
    public void updateStatus(Long id, String status) {
        FollowUp followUp = new FollowUp();
        followUp.setId(id);
        followUp.setStatus(status);
        followUp.setStatusText(STATUS_TEXT_MAP.getOrDefault(status, status));
        followUpMapper.updateById(followUp);
    }

    @Override
    public Long countByPatientId(Long patientId) {
        LambdaQueryWrapper<FollowUp> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowUp::getPatientId, patientId);
        return followUpMapper.selectCount(wrapper);
    }
}