package org.hospital.neuroimmune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hospital.common.model.LoginRequest;
import org.hospital.common.model.PageRequest;
import org.hospital.common.model.PageResult;
import org.hospital.neuroimmune.entity.Doctor;
import org.hospital.neuroimmune.mapper.NeuroimmuneDoctorMapper;
import org.hospital.neuroimmune.mapper.NeuroimmunePatientMapper;
import org.hospital.neuroimmune.service.DoctorService;
import org.hospital.common.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("neuroimmuneDoctorService")
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private NeuroimmuneDoctorMapper doctorMapper;

    @Autowired
    private NeuroimmunePatientMapper patientMapper;

    @Override
    public PageResult<Doctor> getList(PageRequest request) {
        Page<Doctor> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        // 关键词搜索
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.like(Doctor::getName, request.getKeyword())
                   .or().like(Doctor::getPhone, request.getKeyword());
        }
        // 部门过滤
        if (request.getDepartment() != null && !request.getDepartment().isEmpty()) {
            wrapper.eq(Doctor::getDepartment, request.getDepartment());
        }
        wrapper.orderByDesc(Doctor::getCreateTime);

        Page<Doctor> result = doctorMapper.selectPage(page, wrapper);

        // 设置每个医生的患者数量
        result.getRecords().forEach(doctor -> {
            Long count = patientMapper.selectCountByDoctorId(doctor.getId());
            doctor.setPatientCount(count.intValue());
        });

        return new PageResult<>(result.getRecords(), result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public Doctor getById(Long id) {
        Doctor doctor = doctorMapper.selectById(id);
        if (doctor != null) {
            Long count = patientMapper.selectCountByDoctorId(id);
            doctor.setPatientCount(count.intValue());
        }
        return doctor;
    }

    @Override
    public Doctor login(LoginRequest request) {
        Doctor doctor = doctorMapper.selectByPhone(request.getUsername());
        if (doctor != null && doctor.getPassword() != null) {
            if (PasswordUtil.matches(request.getPassword(), doctor.getPassword())) {
                return doctor;
            }
        }
        return null;
    }

    @Override
    public void save(Doctor doctor) {
        if (doctor.getId() == null) {
            if (doctor.getPassword() != null && !doctor.getPassword().startsWith("$2")) {
                doctor.setPassword(PasswordUtil.encode(doctor.getPassword()));
            }
            doctorMapper.insert(doctor);
        } else {
            doctorMapper.updateById(doctor);
        }
    }

    @Override
    public void updatePassword(Long id, String password) {
        LambdaUpdateWrapper<Doctor> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Doctor::getId, id)
                .set(Doctor::getPassword, PasswordUtil.encode(password));
        doctorMapper.update(null, updateWrapper);
    }

    @Override
    public void delete(Long id) {
        doctorMapper.deleteById(id);
    }

    @Override
    public void batchInsert(List<Doctor> doctors) {
        for (Doctor doctor : doctors) {
            if (doctor.getPassword() != null && !doctor.getPassword().startsWith("$2")) {
                doctor.setPassword(PasswordUtil.encode(doctor.getPassword()));
            }
            doctorMapper.insert(doctor);
        }
    }

    @Override
    public Doctor getByPhone(String phone) {
        return doctorMapper.selectByPhone(phone);
    }
}