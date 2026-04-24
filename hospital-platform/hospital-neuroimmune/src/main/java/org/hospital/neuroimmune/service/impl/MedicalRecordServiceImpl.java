package org.hospital.neuroimmune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hospital.common.model.PageRequest;
import org.hospital.common.model.PageResult;
import org.hospital.neuroimmune.entity.MedicalRecord;
import org.hospital.neuroimmune.entity.Patient;
import org.hospital.neuroimmune.entity.Doctor;
import org.hospital.neuroimmune.mapper.MedicalRecordMapper;
import org.hospital.neuroimmune.mapper.NeuroimmunePatientMapper;
import org.hospital.neuroimmune.mapper.NeuroimmuneDoctorMapper;
import org.hospital.neuroimmune.service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MedicalRecordServiceImpl implements MedicalRecordService {

    @Autowired
    private MedicalRecordMapper medicalRecordMapper;

    @Autowired
    private NeuroimmunePatientMapper patientMapper;

    @Autowired
    private NeuroimmuneDoctorMapper doctorMapper;

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
        enrichWithNames(result.getRecords());
        return new PageResult<>(result.getRecords(), result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public PageResult<MedicalRecord> getListByDoctorId(Long doctorId, PageRequest request) {
        Page<MedicalRecord> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicalRecord::getDoctorId, doctorId);

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
        enrichWithNames(result.getRecords());
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

        return wrapper;
    }

    @Override
    public MedicalRecord getById(Long id) {
        MedicalRecord record = medicalRecordMapper.selectById(id);
        if (record != null) enrichSingle(record);
        return record;
    }

    @Override
    public List<MedicalRecord> getByPatientId(Long patientId) {
        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicalRecord::getPatientId, patientId)
               .orderByDesc(MedicalRecord::getDate);
        List<MedicalRecord> list = medicalRecordMapper.selectList(wrapper);
        enrichWithNames(list);
        return list;
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

    private void enrichWithNames(List<MedicalRecord> records) {
        if (records == null || records.isEmpty()) return;

        List<Long> patientIds = records.stream()
                .map(MedicalRecord::getPatientId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<Long> doctorIds = records.stream()
                .map(MedicalRecord::getDoctorId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Patient> patientMap = patientIds.isEmpty() ? Map.of() :
                patientMapper.selectBatchIds(patientIds).stream()
                        .collect(Collectors.toMap(Patient::getId, p -> p, (a, b) -> a));

        Map<Long, Doctor> doctorMap = doctorIds.isEmpty() ? Map.of() :
                doctorMapper.selectBatchIds(doctorIds).stream()
                        .collect(Collectors.toMap(Doctor::getId, d -> d, (a, b) -> a));

        records.forEach(r -> {
            Patient p = patientMap.get(r.getPatientId());
            if (p != null) r.setPatientName(p.getName());
            if (r.getDoctorId() != null) {
                Doctor d = doctorMap.get(r.getDoctorId());
                if (d != null) r.setDoctorName(d.getName());
            }
        });
    }

    private void enrichSingle(MedicalRecord record) {
        if (record.getPatientId() != null) {
            Patient p = patientMapper.selectById(record.getPatientId());
            if (p != null) record.setPatientName(p.getName());
        }
        if (record.getDoctorId() != null) {
            Doctor d = doctorMapper.selectById(record.getDoctorId());
            if (d != null) record.setDoctorName(d.getName());
        }
    }
}