package org.hospital.neuroimmune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hospital.common.enums.RecordStatus;
import org.hospital.common.model.PageRequest;
import org.hospital.common.model.PageResult;
import org.hospital.neuroimmune.entity.MedicalRecord;
import org.hospital.neuroimmune.mapper.MedicalRecordMapper;
import org.hospital.neuroimmune.service.MedicalRecordService;
import org.hospital.neuroimmune.util.EntityNameEnricher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 病历服务实现
 * 已重构：使用 EntityNameEnricher 替代直接操作 Mapper，使用 RecordStatus 枚举替代硬编码状态值
 */
@Service
public class MedicalRecordServiceImpl implements MedicalRecordService {

    @Autowired
    private MedicalRecordMapper medicalRecordMapper;

    @Autowired
    private EntityNameEnricher nameEnricher;

    @Override
    public PageResult<MedicalRecord> getList(PageRequest request) {
        Page<MedicalRecord> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<MedicalRecord> wrapper = buildQueryWrapper(request);
        wrapper.orderByDesc(MedicalRecord::getDate).orderByDesc(MedicalRecord::getCreateTime);

        Page<MedicalRecord> result = medicalRecordMapper.selectPage(page, wrapper);
        nameEnricher.enrichMedicalRecords(result.getRecords());
        return new PageResult<>(result.getRecords(), result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public PageResult<MedicalRecord> getListByDoctorId(Long doctorId, PageRequest request) {
        Page<MedicalRecord> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<>();
        // 通过 patient_doctor_relation 关联查询医生管理的患者的病历
        wrapper.apply("patient_id IN (SELECT patient_id FROM patient_doctor_relation WHERE doctor_id = {0} AND status = 1)", doctorId);

        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            String keyword = request.getKeyword();
            wrapper.and(w -> w.apply("patient_id IN (SELECT id FROM patient WHERE name LIKE {0})", "%" + keyword + "%")
                    .or().like(MedicalRecord::getDiagnosis, keyword)
                    .or().like(MedicalRecord::getHospital, keyword));
        }
        if (request.getType() != null && !request.getType().isEmpty()) {
            wrapper.eq(MedicalRecord::getType, request.getType());
        }
        if (request.getStartDate() != null && !request.getStartDate().isEmpty()) {
            wrapper.ge(MedicalRecord::getDate, request.getStartDate());
        }
        if (request.getEndDate() != null && !request.getEndDate().isEmpty()) {
            wrapper.le(MedicalRecord::getDate, request.getEndDate());
        }

        wrapper.orderByDesc(MedicalRecord::getDate).orderByDesc(MedicalRecord::getCreateTime);

        Page<MedicalRecord> result = medicalRecordMapper.selectPage(page, wrapper);
        nameEnricher.enrichMedicalRecords(result.getRecords());
        return new PageResult<>(result.getRecords(), result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    private LambdaQueryWrapper<MedicalRecord> buildQueryWrapper(PageRequest request) {
        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<>();

        // 默认不显示已取消的记录，除非明确筛选
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            wrapper.eq(MedicalRecord::getStatus, Integer.parseInt(request.getStatus()));
        } else {
            wrapper.ne(MedicalRecord::getStatus, RecordStatus.CANCELLED.getCode());
        }

        if (request.getPatientId() != null) {
            wrapper.eq(MedicalRecord::getPatientId, request.getPatientId());
        }
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            String keyword = request.getKeyword();
            wrapper.and(w -> w.apply("patient_id IN (SELECT id FROM patient WHERE name LIKE {0})", "%" + keyword + "%")
                    .or().like(MedicalRecord::getDiagnosis, keyword)
                    .or().like(MedicalRecord::getHospital, keyword));
        }
        if (request.getType() != null && !request.getType().isEmpty()) {
            wrapper.eq(MedicalRecord::getType, request.getType());
        }
        if (request.getStartDate() != null && !request.getStartDate().isEmpty()) {
            wrapper.ge(MedicalRecord::getDate, request.getStartDate());
        }
        if (request.getEndDate() != null && !request.getEndDate().isEmpty()) {
            wrapper.le(MedicalRecord::getDate, request.getEndDate());
        }
        if (request.getRelatedEpisodeId() != null) {
            wrapper.eq(MedicalRecord::getRelatedEpisodeId, request.getRelatedEpisodeId());
        }

        return wrapper;
    }

    @Override
    public MedicalRecord getById(Long id) {
        MedicalRecord record = medicalRecordMapper.selectById(id);
        nameEnricher.enrichSingleMedicalRecord(record);
        return record;
    }

    @Override
    public List<MedicalRecord> getByPatientId(Long patientId) {
        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicalRecord::getPatientId, patientId)
               .orderByDesc(MedicalRecord::getDate);
        List<MedicalRecord> list = medicalRecordMapper.selectList(wrapper);
        nameEnricher.enrichMedicalRecords(list);
        return list;
    }

    @Override
    public void save(MedicalRecord record) {
        if (record.getStatus() == null) {
            record.setStatus(RecordStatus.ONGOING.getCode());
        }
        if (record.getId() == null) {
            medicalRecordMapper.insert(record);
        } else {
            medicalRecordMapper.updateById(record);
        }
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        MedicalRecord record = new MedicalRecord();
        record.setId(id);
        record.setStatus(status);
        medicalRecordMapper.updateById(record);
    }

    @Override
    public void cancel(Long id) {
        updateStatus(id, RecordStatus.CANCELLED.getCode());
    }

    @Override
    public Long countByPatientId(Long patientId) {
        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicalRecord::getPatientId, patientId);
        Object count = medicalRecordMapper.selectCount(wrapper);
        return count != null ? Long.valueOf(count.toString()) : 0L;
    }

    @Override
    public Long countByDoctorId(Long doctorId) {
        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicalRecord::getDoctorId, doctorId);
        Object count = medicalRecordMapper.selectCount(wrapper);
        return count != null ? Long.valueOf(count.toString()) : 0L;
    }

    @Override
    public void deleteByPatientId(Long patientId) {
        LambdaUpdateWrapper<MedicalRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MedicalRecord::getPatientId, patientId)
                     .set(MedicalRecord::getIsDeleted, 1);
        medicalRecordMapper.update(null, updateWrapper);
    }
}