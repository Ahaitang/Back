package org.hospital.neuroimmune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hospital.common.model.PageRequest;
import org.hospital.common.model.PageResult;
import org.hospital.neuroimmune.entity.Medication;
import org.hospital.neuroimmune.mapper.MedicationMapper;
import org.hospital.neuroimmune.service.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MedicationServiceImpl implements MedicationService {

    @Autowired
    private MedicationMapper medicationMapper;

    @Override
    public PageResult<Medication> getList(PageRequest request) {
        Page<Medication> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<Medication> wrapper = buildQueryWrapper(request);
        wrapper.orderByDesc(Medication::getDate).orderByDesc(Medication::getCreateTime);

        Page<Medication> result = medicationMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public PageResult<Medication> getListByDoctorId(Long doctorId, PageRequest request) {
        Page<Medication> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<Medication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Medication::getDoctorId, doctorId);

        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(Medication::getPatientName, request.getKeyword())
                    .or().like(Medication::getMedicationName, request.getKeyword()));
        }
        if (request.getStartDate() != null && !request.getStartDate().isEmpty()) {
            wrapper.ge(Medication::getDate, request.getStartDate());
        }
        if (request.getEndDate() != null && !request.getEndDate().isEmpty()) {
            wrapper.le(Medication::getDate, request.getEndDate());
        }

        wrapper.orderByDesc(Medication::getDate).orderByDesc(Medication::getCreateTime);

        Page<Medication> result = medicationMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    private LambdaQueryWrapper<Medication> buildQueryWrapper(PageRequest request) {
        LambdaQueryWrapper<Medication> wrapper = new LambdaQueryWrapper<>();

        if (request.getPatientId() != null) {
            wrapper.eq(Medication::getPatientId, request.getPatientId());
        }
        if (request.getDoctorId() != null) {
            wrapper.eq(Medication::getDoctorId, request.getDoctorId());
        }
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(Medication::getPatientName, request.getKeyword())
                    .or().like(Medication::getMedicationName, request.getKeyword())
                    .or().like(Medication::getDoctorName, request.getKeyword()));
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
        return medicationMapper.selectList(wrapper);
    }

    @Override
    public List<Medication> getByDoctorId(Long doctorId) {
        LambdaQueryWrapper<Medication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Medication::getDoctorId, doctorId)
               .orderByDesc(Medication::getDate);
        return medicationMapper.selectList(wrapper);
    }

    @Override
    public List<Medication> getAllMedications() {
        return medicationMapper.selectAllMedications();
    }

    @Override
    public void save(Medication medication) {
        if (medication.getId() == null) {
            medicationMapper.insert(medication);
        } else {
            medicationMapper.updateById(medication);
        }
    }

    @Override
    public void delete(Long id) {
        medicationMapper.deleteById(id);
    }

    @Override
    public Long countByDoctorId(Long doctorId) {
        return medicationMapper.selectCountByDoctorId(doctorId);
    }

    @Override
    public Long countByPatientId(Long patientId) {
        LambdaQueryWrapper<Medication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Medication::getPatientId, patientId);
        return medicationMapper.selectCount(wrapper);
    }
}