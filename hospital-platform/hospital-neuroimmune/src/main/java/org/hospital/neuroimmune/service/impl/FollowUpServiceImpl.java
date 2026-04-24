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

import java.util.List;

@Service
public class FollowUpServiceImpl implements FollowUpService {

    @Autowired
    private FollowUpMapper followUpMapper;

    // 状态常量: 0-进行中, 1-完成, 2-取消
    public static final int STATUS_ONGOING = 0;
    public static final int STATUS_COMPLETED = 1;
    public static final int STATUS_CANCELLED = 2;

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

        // 默认不显示已取消的记录，除非明确筛选
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            wrapper.eq(FollowUp::getStatus, Integer.parseInt(request.getStatus()));
        } else {
            // 默认只显示进行中和已完成的记录
            wrapper.ne(FollowUp::getStatus, STATUS_CANCELLED);
        }

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
            followUp.setStatus(STATUS_ONGOING);
        }
        if (followUp.getId() == null) {
            followUpMapper.insert(followUp);
        } else {
            followUpMapper.updateById(followUp);
        }
    }

    @Override
    @CacheEvict(value = {"neuro-followup", "neuro-stats"}, allEntries = true)
    public void updateStatus(Long id, Integer status) {
        FollowUp followUp = new FollowUp();
        followUp.setId(id);
        followUp.setStatus(status);
        followUpMapper.updateById(followUp);
    }

    @Override
    @CacheEvict(value = {"neuro-followup", "neuro-stats"}, allEntries = true)
    public void cancel(Long id) {
        updateStatus(id, STATUS_CANCELLED);
    }

    @Override
    @Cacheable(value = "neuro-stats", key = "'followup:pending'")
    public Long getPendingCount() {
        LambdaQueryWrapper<FollowUp> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowUp::getStatus, STATUS_ONGOING);
        Long count = followUpMapper.selectCount(wrapper);
        return count != null ? count : 0L;
    }

    @Override
    @Cacheable(value = "neuro-stats", key = "'followup:pending:doctor:' + #doctorId")
    public Long getPendingCountByDoctorId(Long doctorId) {
        LambdaQueryWrapper<FollowUp> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowUp::getDoctorId, doctorId)
               .eq(FollowUp::getStatus, STATUS_ONGOING);
        Long count = followUpMapper.selectCount(wrapper);
        return count != null ? count : 0L;
    }

    @Override
    public List<FollowUp> getPendingByDoctorId(Long doctorId) {
        LambdaQueryWrapper<FollowUp> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowUp::getDoctorId, doctorId)
               .eq(FollowUp::getStatus, STATUS_ONGOING)
               .orderByAsc(FollowUp::getDate);
        return followUpMapper.selectList(wrapper);
    }

    @Override
    public Long getPendingCountByPatientId(Long patientId) {
        LambdaQueryWrapper<FollowUp> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowUp::getPatientId, patientId)
               .eq(FollowUp::getStatus, STATUS_ONGOING);
        Long count = followUpMapper.selectCount(wrapper);
        return count != null ? count : 0L;
    }

    @Override
    public Long countByPatientId(Long patientId) {
        LambdaQueryWrapper<FollowUp> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowUp::getPatientId, patientId);
        Long count = followUpMapper.selectCount(wrapper);
        return count != null ? count : 0L;
    }
}