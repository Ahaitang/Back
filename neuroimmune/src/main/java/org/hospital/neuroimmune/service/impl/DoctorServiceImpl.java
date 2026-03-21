package org.hospital.neuroimmune.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.hospital.neuroimmune.dto.LoginRequest;
import org.hospital.neuroimmune.dto.PageRequest;
import org.hospital.neuroimmune.dto.PageResult;
import org.hospital.neuroimmune.entity.Doctor;
import org.hospital.neuroimmune.mapper.DoctorMapper;
import org.hospital.neuroimmune.mapper.PatientMapper;
import org.hospital.neuroimmune.service.DoctorService;
import org.hospital.neuroimmune.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private PatientMapper patientMapper;

    @Override
    public PageResult<Doctor> getList(PageRequest request) {
        List<Doctor> list = doctorMapper.selectList(request);
        // 设置每个医生的患者数量
        list.forEach(doctor -> {
            Long count = patientMapper.selectCountByDoctorId(doctor.getId());
            doctor.setPatientCount(count.intValue());
        });
        Long total = doctorMapper.selectCount(request);
        return new PageResult<>(list, total, request.getPageNum(), request.getPageSize());
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
        // 用户名可以是手机号
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
            // 新增时加密密码
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
            // 加密密码
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