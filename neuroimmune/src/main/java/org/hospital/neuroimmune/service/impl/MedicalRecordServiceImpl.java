package org.hospital.neuroimmune.service.impl;

import org.hospital.neuroimmune.dto.PageRequest;
import org.hospital.neuroimmune.dto.PageResult;
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

    @Override
    public PageResult<MedicalRecord> getList(PageRequest request) {
        List<MedicalRecord> list = medicalRecordMapper.selectList(request);
        Long total = medicalRecordMapper.selectCount(request);
        return new PageResult<>(list, total, request.getPageNum(), request.getPageSize());
    }

    @Override
    public PageResult<MedicalRecord> getListByDoctorId(Long doctorId, PageRequest request) {
        List<MedicalRecord> list = medicalRecordMapper.selectListByDoctorId(doctorId, request);
        Long total = medicalRecordMapper.selectCountByDoctorId(doctorId, request);
        return new PageResult<>(list, total, request.getPageNum(), request.getPageSize());
    }

    @Override
    public MedicalRecord getById(Long id) {
        return medicalRecordMapper.selectById(id);
    }

    @Override
    public void save(MedicalRecord record) {
        if (record.getId() == null) {
            medicalRecordMapper.insert(record);
        } else {
            medicalRecordMapper.updateById(record);
        }
    }

    @Override
    public void delete(Long id) {
        medicalRecordMapper.deleteById(id);
    }
}