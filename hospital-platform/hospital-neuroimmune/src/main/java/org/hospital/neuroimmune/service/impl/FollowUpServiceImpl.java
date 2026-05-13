package org.hospital.neuroimmune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hospital.common.enums.RecordStatus;
import org.hospital.common.model.PageRequest;
import org.hospital.common.model.PageResult;
import org.hospital.neuroimmune.entity.FollowUp;
import org.hospital.neuroimmune.mapper.FollowUpMapper;
import org.hospital.neuroimmune.service.FollowUpService;
import org.hospital.neuroimmune.service.PatientDoctorRelationService;
import org.hospital.neuroimmune.entity.PatientDoctorRelation;
import org.hospital.neuroimmune.util.EntityNameEnricher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 随访服务实现
 * 已重构：使用 EntityNameEnricher 替代直接操作 Mapper，使用 RecordStatus 枚举替代硬编码状态值
 */
@Service
public class FollowUpServiceImpl implements FollowUpService {

    @Autowired
    private FollowUpMapper followUpMapper;

    @Autowired
    private EntityNameEnricher nameEnricher;

    @Autowired
    private PatientDoctorRelationService relationService;

    @Override
    public PageResult<FollowUp> getList(PageRequest request) {
        Page<FollowUp> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<FollowUp> wrapper = buildQueryWrapper(request);
        wrapper.orderByDesc(FollowUp::getCreateTime);

        Page<FollowUp> result = followUpMapper.selectPage(page, wrapper);
        nameEnricher.enrichFollowUps(result.getRecords());
        return new PageResult<>(result.getRecords(), result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public PageResult<FollowUp> getListByDoctorId(Long doctorId, PageRequest request) {
        Page<FollowUp> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<FollowUp> wrapper = buildQueryWrapper(request);
        wrapper.eq(FollowUp::getDoctorId, doctorId);
        wrapper.orderByDesc(FollowUp::getCreateTime);

        Page<FollowUp> result = followUpMapper.selectPage(page, wrapper);
        nameEnricher.enrichFollowUps(result.getRecords());
        return new PageResult<>(result.getRecords(), result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    private LambdaQueryWrapper<FollowUp> buildQueryWrapper(PageRequest request) {
        LambdaQueryWrapper<FollowUp> wrapper = new LambdaQueryWrapper<>();

        // 默认不显示已取消的记录，除非明确筛选
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            wrapper.eq(FollowUp::getStatus, Integer.parseInt(request.getStatus()));
        } else {
            // 默认只显示进行中和已完成的记录
            wrapper.ne(FollowUp::getStatus, RecordStatus.CANCELLED.getCode());
        }

        if (request.getPatientId() != null) {
            wrapper.eq(FollowUp::getPatientId, request.getPatientId());
        }
        if (request.getDoctorId() != null) {
            wrapper.eq(FollowUp::getDoctorId, request.getDoctorId());
        }
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            String keyword = request.getKeyword();
            wrapper.and(w -> w.apply("patient_id IN (SELECT id FROM patient WHERE name LIKE {0})", "%" + keyword + "%")
                    .or().apply("doctor_id IN (SELECT id FROM doctor WHERE name LIKE {0})", "%" + keyword + "%")
                    .or().like(FollowUp::getExaminationItems, keyword)
                    .or().like(FollowUp::getNotes, keyword));
        }
        // 按 followUpExamTypeId 筛选（如果传入了类型名称）
        if (request.getType() != null && !request.getType().isEmpty()) {
            wrapper.apply("follow_up_exam_type_id IN (SELECT id FROM dict_common WHERE dict_type = 'followUpExamType' AND name = {0})", request.getType());
        }

        return wrapper;
    }

    @Override
    public FollowUp getById(Long id) {
        FollowUp followUp = followUpMapper.selectById(id);
        nameEnricher.enrichSingleFollowUp(followUp);
        return followUp;
    }

    @Override
    @CacheEvict(value = {"neuro-followup", "neuro-stats"}, allEntries = true)
    public void save(FollowUp followUp) {
        if (followUp.getStatus() == null) {
            followUp.setStatus(RecordStatus.ONGOING.getCode());
        }
        // 新增时，如果 doctorId 为空，自动从患者-医生关系中获取
        if (followUp.getId() == null && followUp.getDoctorId() == null && followUp.getPatientId() != null) {
            PatientDoctorRelation relation = relationService.getActiveDoctor(followUp.getPatientId());
            if (relation != null) {
                followUp.setDoctorId(relation.getDoctorId());
            }
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
        updateStatus(id, RecordStatus.CANCELLED.getCode());
    }

    @Override
    @Cacheable(value = "neuro-stats", key = "'followup:pending'")
    public Long getPendingCount() {
        LambdaQueryWrapper<FollowUp> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowUp::getStatus, RecordStatus.ONGOING.getCode());
        Object count = followUpMapper.selectCount(wrapper);
        return count != null ? Long.valueOf(count.toString()) : 0L;
    }

    @Override
    @Cacheable(value = "neuro-stats", key = "'followup:pending:doctor:' + #doctorId")
    public Long getPendingCountByDoctorId(Long doctorId) {
        LambdaQueryWrapper<FollowUp> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowUp::getDoctorId, doctorId)
               .eq(FollowUp::getStatus, RecordStatus.ONGOING.getCode());
        Object count = followUpMapper.selectCount(wrapper);
        return count != null ? Long.valueOf(count.toString()) : 0L;
    }

    @Override
    public List<FollowUp> getPendingByDoctorId(Long doctorId) {
        LambdaQueryWrapper<FollowUp> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowUp::getDoctorId, doctorId)
               .eq(FollowUp::getStatus, RecordStatus.ONGOING.getCode())
               .orderByDesc(FollowUp::getCreateTime);
        List<FollowUp> list = followUpMapper.selectList(wrapper);
        nameEnricher.enrichFollowUps(list);
        return list;
    }

    @Override
    public Long getPendingCountByPatientId(Long patientId) {
        LambdaQueryWrapper<FollowUp> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowUp::getPatientId, patientId)
               .eq(FollowUp::getStatus, RecordStatus.ONGOING.getCode());
        Object count = followUpMapper.selectCount(wrapper);
        return count != null ? Long.valueOf(count.toString()) : 0L;
    }

    @Override
    @Cacheable(value = "neuro-stats", key = "'followup:completed'")
    public Long getCompletedCount() {
        LambdaQueryWrapper<FollowUp> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowUp::getStatus, RecordStatus.COMPLETED.getCode());
        Object count = followUpMapper.selectCount(wrapper);
        return count != null ? Long.valueOf(count.toString()) : 0L;
    }

    @Override
    @Cacheable(value = "neuro-stats", key = "'followup:completed:doctor:' + #doctorId")
    public Long getCompletedCountByDoctorId(Long doctorId) {
        LambdaQueryWrapper<FollowUp> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowUp::getDoctorId, doctorId)
               .eq(FollowUp::getStatus, RecordStatus.COMPLETED.getCode());
        Object count = followUpMapper.selectCount(wrapper);
        return count != null ? Long.valueOf(count.toString()) : 0L;
    }

    @Override
    public Long countByPatientId(Long patientId) {
        LambdaQueryWrapper<FollowUp> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowUp::getPatientId, patientId);
        Object count = followUpMapper.selectCount(wrapper);
        return count != null ? Long.valueOf(count.toString()) : 0L;
    }

    @Override
    @CacheEvict(value = {"neuro-followup", "neuro-stats"}, allEntries = true)
    public void cancelByPatientId(Long patientId) {
        // 批量取消患者所有随访记录
        LambdaUpdateWrapper<FollowUp> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(FollowUp::getPatientId, patientId)
                     .ne(FollowUp::getStatus, RecordStatus.CANCELLED.getCode())
                     .set(FollowUp::getStatus, RecordStatus.CANCELLED.getCode());
        followUpMapper.update(null, updateWrapper);
    }

    @Override
    public Map<Long, Boolean> batchGetPendingStatus(List<Long> patientIds) {
        Map<Long, Boolean> result = new HashMap<>();

        // 添加null/empty检查
        if (patientIds == null || patientIds.isEmpty()) {
            return result;
        }

        // 初始化所有患者ID为false
        for (Long patientId : patientIds) {
            result.put(patientId, false);
        }
        // 查询有待随访记录的患者ID
        if (!patientIds.isEmpty()) {
            LambdaQueryWrapper<FollowUp> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(FollowUp::getPatientId, patientIds)
                   .eq(FollowUp::getStatus, RecordStatus.ONGOING.getCode())
                   .select(FollowUp::getPatientId)
                   .groupBy(FollowUp::getPatientId);
            List<FollowUp> pendingFollowUps = followUpMapper.selectList(wrapper);
            for (FollowUp followUp : pendingFollowUps) {
                result.put(followUp.getPatientId(), true);
            }
        }
        return result;
    }
}