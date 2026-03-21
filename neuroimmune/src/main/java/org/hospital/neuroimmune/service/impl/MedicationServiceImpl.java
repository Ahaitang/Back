package org.hospital.neuroimmune.service.impl;

import org.hospital.neuroimmune.dto.PageRequest;
import org.hospital.neuroimmune.dto.PageResult;
import org.hospital.neuroimmune.entity.Medication;
import org.hospital.neuroimmune.mapper.MedicationMapper;
import org.hospital.neuroimmune.service.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicationServiceImpl implements MedicationService {

    @Autowired
    private MedicationMapper medicationMapper;

    @Override
    public PageResult<Medication> getList(PageRequest request) {
        List<Medication> list = medicationMapper.selectList(request);
        Long total = medicationMapper.selectCount(request);
        return new PageResult<>(list, total, request.getPageNum(), request.getPageSize());
    }

    @Override
    public Medication getById(Long id) {
        return medicationMapper.selectById(id);
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
    public PageResult<Medication> getListByDoctorId(Long doctorId, PageRequest request) {
        List<Medication> list = medicationMapper.selectListByDoctorId(doctorId, request);
        Long total = medicationMapper.selectCountByDoctorId(doctorId);
        return new PageResult<>(list, total, request.getPageNum(), request.getPageSize());
    }
}