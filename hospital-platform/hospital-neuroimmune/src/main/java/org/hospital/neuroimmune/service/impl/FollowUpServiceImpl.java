package org.hospital.neuroimmune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hospital.common.model.PageRequest;
import org.hospital.common.model.PageResult;
import org.hospital.neuroimmune.entity.FollowUp;
import org.hospital.neuroimmune.entity.Patient;
import org.hospital.neuroimmune.entity.Doctor;
import org.hospital.neuroimmune.mapper.FollowUpMapper;
import org.hospital.neuroimmune.mapper.NeuroimmunePatientMapper;
import org.hospital.neuroimmune.mapper.NeuroimmuneDoctorMapper;
import org.hospital.neuroimmune.service.FollowUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class FollowUpServiceImpl implements FollowUpService {

    @Autowired
    private FollowUpMapper followUpMapper;

    @Autowired
    private NeuroimmunePatientMapper patientMapper;

    @Autowired
    private NeuroimmuneDoctorMapper doctorMapper;

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
        enrichWithNames(result.getRecords());
        return new PageResult<>(result.getRecords(), result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public PageResult<FollowUp> getListByDoctorId(Long doctorId, PageRequest request) {
        Page<FollowUp> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<FollowUp> wrapper = buildQueryWrapper(request);
        wrapper.eq(FollowUp::getDoctorId, doctorId);
        wrapper.orderByDesc(FollowUp::getDate).orderByDesc(FollowUp::getCreateTime);

        Page<FollowUp> result = followUpMapper.selectPage(page, wrapper);
        enrichWithNames(result.getRecords());
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
            String keyword = request.getKeyword();
            wrapper.and(w -> w.apply("patient_id IN (SELECT id FROM patient WHERE name LIKE {0})", "%" + keyword + "%")
                    .or().apply("doctor_id IN (SELECT id FROM doctor WHERE name LIKE {0})", "%" + keyword + "%")
                    .or().like(FollowUp::getProject, keyword));
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
        List<FollowUp> list = followUpMapper.selectList(wrapper);
        enrichWithNames(list);
        return list;
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

    @Override
    @CacheEvict(value = {"neuro-followup", "neuro-stats"}, allEntries = true)
    public void cancelByPatientId(Long patientId) {
        // 批量取消患者所有随访记录
        LambdaUpdateWrapper<FollowUp> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(FollowUp::getPatientId, patientId)
                     .ne(FollowUp::getStatus, STATUS_CANCELLED)
                     .set(FollowUp::getStatus, STATUS_CANCELLED);
        followUpMapper.update(null, updateWrapper);
    }

    private void enrichWithNames(List<FollowUp> followUps) {
        if (followUps == null || followUps.isEmpty()) return;

        List<Long> patientIds = followUps.stream()
                .map(FollowUp::getPatientId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<Long> doctorIds = followUps.stream()
                .map(FollowUp::getDoctorId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Patient> patientMap = patientIds.isEmpty() ? Map.of() :
                patientMapper.selectBatchIds(patientIds).stream()
                        .collect(Collectors.toMap(Patient::getId, p -> p, (a, b) -> a));

        Map<Long, Doctor> doctorMap = doctorIds.isEmpty() ? Map.of() :
                doctorMapper.selectBatchIds(doctorIds).stream()
                        .collect(Collectors.toMap(Doctor::getId, d -> d, (a, b) -> a));

        followUps.forEach(fu -> {
            Patient p = patientMap.get(fu.getPatientId());
            if (p != null) {
                fu.setPatientName(p.getName());
                fu.setPatientGender(p.getGender());
                fu.setPatientAge(p.getAge());
            }
            if (fu.getDoctorId() != null) {
                Doctor d = doctorMap.get(fu.getDoctorId());
                if (d != null) fu.setDoctorName(d.getName());
            }
        });
    }
}