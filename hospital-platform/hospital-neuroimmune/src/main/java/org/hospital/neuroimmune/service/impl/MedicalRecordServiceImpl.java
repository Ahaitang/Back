package org.hospital.neuroimmune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hospital.common.model.PageRequest;
import org.hospital.common.model.PageResult;
import org.hospital.neuroimmune.entity.MedicalRecord;
import org.hospital.neuroimmune.mapper.MedicalRecordMapper;
import org.hospital.neuroimmune.service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicalRecordServiceImpl implements MedicalRecordService {

    @Autowired
    private MedicalRecordMapper medicalRecordMapper;

    // 状态常量: 0-进行中, 1-完成, 2-取消
    public static final int STATUS_ONGOING = 0;
    public static final int STATUS_COMPLETED = 1;
    public static final int STATUS_CANCELLED = 2;

    @Override
    public PageResult<MedicalRecord> getList(PageRequest request) {
        Page<MedicalRecord> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<MedicalRecord> wrapper = buildQueryWrapper(request);
        wrapper.orderByDesc(MedicalRecord::getDate).orderByDesc(MedicalRecord::getCreateTime);

        Page<MedicalRecord> result = medicalRecordMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public PageResult<MedicalRecord> getListByDoctorId(Long doctorId, PageRequest request) {
        // 通过医生的 patient 表关联查询，这里用子查询实现
        Page<MedicalRecord> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.inSql(MedicalRecord::getPatientId,
                "SELECT patient_id FROM patient_doctor_relation WHERE doctor_id = " + doctorId + " AND status = 'active'");

        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(MedicalRecord::getPatientName, request.getKeyword())
                    .or().like(MedicalRecord::getDiagnosis, request.getKeyword())
                    .or().like(MedicalRecord::getHospital, request.getKeyword()));
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
        return new PageResult<>(result.getRecords(), result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    private LambdaQueryWrapper<MedicalRecord> buildQueryWrapper(PageRequest request) {
        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<>();

        // 默认不显示已取消的记录，除非明确筛选
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            wrapper.eq(MedicalRecord::getStatus, Integer.parseInt(request.getStatus()));
        } else {
            wrapper.ne(MedicalRecord::getStatus, STATUS_CANCELLED);
        }

        if (request.getPatientId() != null) {
            wrapper.eq(MedicalRecord::getPatientId, request.getPatientId());
        }
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(MedicalRecord::getPatientName, request.getKeyword())
                    .or().like(MedicalRecord::getDiagnosis, request.getKeyword())
                    .or().like(MedicalRecord::getHospital, request.getKeyword()));
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

        return wrapper;
    }

    @Override
    public MedicalRecord getById(Long id) {
        return medicalRecordMapper.selectById(id);
    }

    @Override
    public List<MedicalRecord> getByPatientId(Long patientId) {
        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicalRecord::getPatientId, patientId)
               .orderByDesc(MedicalRecord::getDate);
        return medicalRecordMapper.selectList(wrapper);
    }

    @Override
    public void save(MedicalRecord record) {
        if (record.getStatus() == null) {
            record.setStatus(STATUS_ONGOING);
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
        updateStatus(id, STATUS_CANCELLED);
    }

    @Override
    public Long countByPatientId(Long patientId) {
        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicalRecord::getPatientId, patientId);
        return medicalRecordMapper.selectCount(wrapper);
    }
}