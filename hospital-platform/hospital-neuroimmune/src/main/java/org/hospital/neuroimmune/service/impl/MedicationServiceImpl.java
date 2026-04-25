package org.hospital.neuroimmune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hospital.common.enums.RecordStatus;
import org.hospital.common.model.PageRequest;
import org.hospital.common.model.PageResult;
import org.hospital.neuroimmune.entity.Medication;
import org.hospital.neuroimmune.mapper.MedicationMapper;
import org.hospital.neuroimmune.service.MedicationService;
import org.hospital.neuroimmune.util.EntityNameEnricher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用药服务实现
 * 已重构：使用 EntityNameEnricher 替代直接操作 Mapper，使用 RecordStatus 枚举替代硬编码状态值
 */
@Service
public class MedicationServiceImpl implements MedicationService {

    @Autowired
    private MedicationMapper medicationMapper;

    @Autowired
    private EntityNameEnricher nameEnricher;

    @Override
    public PageResult<Medication> getList(PageRequest request) {
        Page<Medication> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<Medication> wrapper = buildQueryWrapper(request);
        wrapper.orderByDesc(Medication::getDate).orderByDesc(Medication::getCreateTime);

        Page<Medication> result = medicationMapper.selectPage(page, wrapper);
        nameEnricher.enrichMedications(result.getRecords());
        return new PageResult<>(result.getRecords(), result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public PageResult<Medication> getListByDoctorId(Long doctorId, PageRequest request) {
        Page<Medication> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<Medication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Medication::getDoctorId, doctorId);

        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            String keyword = request.getKeyword();
            wrapper.and(w -> w.apply("patient_id IN (SELECT id FROM patient WHERE name LIKE {0})", "%" + keyword + "%")
                    .or().like(Medication::getMedicationName, keyword));
        }
        if (request.getStartDate() != null && !request.getStartDate().isEmpty()) {
            wrapper.ge(Medication::getDate, request.getStartDate());
        }
        if (request.getEndDate() != null && !request.getEndDate().isEmpty()) {
            wrapper.le(Medication::getDate, request.getEndDate());
        }

        wrapper.orderByDesc(Medication::getDate).orderByDesc(Medication::getCreateTime);

        Page<Medication> result = medicationMapper.selectPage(page, wrapper);
        nameEnricher.enrichMedications(result.getRecords());
        return new PageResult<>(result.getRecords(), result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    private LambdaQueryWrapper<Medication> buildQueryWrapper(PageRequest request) {
        LambdaQueryWrapper<Medication> wrapper = new LambdaQueryWrapper<>();

        // 默认不显示已取消的记录，除非明确筛选
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            wrapper.eq(Medication::getStatus, Integer.parseInt(request.getStatus()));
        } else {
            wrapper.ne(Medication::getStatus, RecordStatus.CANCELLED.getCode());
        }

        if (request.getPatientId() != null) {
            wrapper.eq(Medication::getPatientId, request.getPatientId());
        }
        if (request.getDoctorId() != null) {
            wrapper.eq(Medication::getDoctorId, request.getDoctorId());
        }
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            String keyword = request.getKeyword();
            wrapper.and(w -> w.apply("patient_id IN (SELECT id FROM patient WHERE name LIKE {0})", "%" + keyword + "%")
                    .or().apply("doctor_id IN (SELECT id FROM doctor WHERE name LIKE {0})", "%" + keyword + "%")
                    .or().like(Medication::getMedicationName, keyword));
        }
        if (request.getStartDate() != null && !request.getStartDate().isEmpty()) {
            wrapper.ge(Medication::getDate, request.getStartDate());
        }
        if (request.getEndDate() != null && !request.getEndDate().isEmpty()) {
            wrapper.le(Medication::getDate, request.getEndDate());
        }

        return wrapper;
    }

    @Override
    public Medication getById(Long id) {
        return medicationMapper.selectById(id);
    }

    @Override
    public List<Medication> getByPatientId(Long patientId) {
        LambdaQueryWrapper<Medication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Medication::getPatientId, patientId)
               .orderByDesc(Medication::getDate);
        List<Medication> list = medicationMapper.selectList(wrapper);
        nameEnricher.enrichMedications(list);
        return list;
    }

    @Override
    public List<Medication> getByDoctorId(Long doctorId) {
        LambdaQueryWrapper<Medication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Medication::getDoctorId, doctorId)
               .orderByDesc(Medication::getDate);
        List<Medication> list = medicationMapper.selectList(wrapper);
        nameEnricher.enrichMedications(list);
        return list;
    }

    @Override
    public List<Medication> getAllMedications() {
        return medicationMapper.selectAllMedications();
    }

    @Override
    public void save(Medication medication) {
        if (medication.getStatus() == null) {
            medication.setStatus(RecordStatus.ONGOING.getCode());
        }
        if (medication.getId() == null) {
            medicationMapper.insert(medication);
        } else {
            medicationMapper.updateById(medication);
        }
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Medication medication = new Medication();
        medication.setId(id);
        medication.setStatus(status);
        medicationMapper.updateById(medication);
    }

    @Override
    public void cancel(Long id) {
        updateStatus(id, RecordStatus.CANCELLED.getCode());
    }

    @Override
    public Long countByDoctorId(Long doctorId) {
        LambdaQueryWrapper<Medication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Medication::getDoctorId, doctorId);
        return medicationMapper.selectCount(wrapper);
    }

    @Override
    public Long countByPatientId(Long patientId) {
        LambdaQueryWrapper<Medication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Medication::getPatientId, patientId);
        return medicationMapper.selectCount(wrapper);
    }

    @Override
    public void deleteByPatientId(Long patientId) {
        LambdaUpdateWrapper<Medication> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Medication::getPatientId, patientId)
                     .set(Medication::getIsDeleted, 1);
        medicationMapper.update(null, updateWrapper);
    }
}