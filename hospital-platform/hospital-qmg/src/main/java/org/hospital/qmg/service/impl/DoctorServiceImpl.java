package org.hospital.qmg.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.hospital.common.util.PasswordUtil;
import org.hospital.qmg.entity.Doctor;
import org.hospital.qmg.mapper.QmgDoctorMapper;
import org.hospital.qmg.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 医生Service实现类
 */
@Slf4j
@Service("qmgDoctorService")
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private QmgDoctorMapper doctorMapper;

    @Override
    public Doctor findByUsername(String username) {
        log.info("根据用户名查询医生: {}", username);
        return doctorMapper.findByUsername(username);
    }

    @Override
    public Doctor findByEmployeeNumber(String employeeNumber) {
        log.info("根据工号查询医生: {}", employeeNumber);
        return doctorMapper.findByEmployeeNumber(employeeNumber);
    }

    @Override
    public Doctor findById(Integer id) {
        log.info("根据ID查询医生: {}", id);
        return doctorMapper.findById(id);
    }

    @Override
    public Doctor login(String username, String password) {
        log.info("医生登录请求: username={}, passwordLength={}", username, password != null ? password.length() : 0);
        Doctor doctor = doctorMapper.findByUsername(username);
        
        if (doctor == null) {
            log.warn("医生不存在: {}", username);
            return null;
        }

        // 验证密码
        boolean passwordMatches = PasswordUtil.matches(password, doctor.getPassword());
        log.debug("密码验证结果: username={}, matches={}, storedPasswordLength={}", 
            username, passwordMatches, doctor.getPassword() != null ? doctor.getPassword().length() : 0);
        
        if (passwordMatches) {
            log.info("医生登录成功: username={}, employeeNumber={}, level={}", 
                username, doctor.getEmployeeNumber(), doctor.getLevel());
            // 不返回密码
            doctor.setPassword(null);
            return doctor;
        } else {
            log.warn("密码错误: username={}, employeeNumber={}", username, doctor.getEmployeeNumber());
            return null;
        }
    }

    @Override
    public void save(Doctor doctor) {
        log.info("新增医生: {}", doctor.getUsername());
        // 加密密码
        String encodedPassword = PasswordUtil.encode(doctor.getPassword());
        doctor.setPassword(encodedPassword);
        // 如果没有设置权限等级，默认为2（普通医生）
        if (doctor.getLevel() == null) {
            doctor.setLevel(2);
        }
        doctor.setCreateTime(LocalDateTime.now());
        doctor.setUpdateTime(LocalDateTime.now());
        doctorMapper.insert(doctor);
    }

    @Override
    public void update(Doctor doctor) {
        log.info("更新医生信息: {}", doctor.getUsername());
        // 仅当传入新密码（明文）时才加密并更新；为 null 表示不修改密码，Mapper 会跳过 password 列
        if (doctor.getPassword() != null && !doctor.getPassword().isEmpty()) {
            String encodedPassword = PasswordUtil.encode(doctor.getPassword());
            doctor.setPassword(encodedPassword);
        }
        doctor.setUpdateTime(LocalDateTime.now());
        doctorMapper.update(doctor);
    }

    @Override
    public int batchRegister(List<Doctor> doctors) {
        log.info("批量注册医生，数量: {}", doctors.size());
        
        LocalDateTime now = LocalDateTime.now();
        
        // 加密所有医生的密码
        for (Doctor doctor : doctors) {
            if (doctor.getPassword() != null && !doctor.getPassword().isEmpty()) {
                String originalPassword = doctor.getPassword();
                String encodedPassword = PasswordUtil.encode(originalPassword);
                doctor.setPassword(encodedPassword);
                log.debug("医生密码已加密: username={}, employeeNumber={}, 原始密码长度={}, 加密后长度={}", 
                    doctor.getUsername(), doctor.getEmployeeNumber(), 
                    originalPassword.length(), encodedPassword.length());
            } else {
                log.warn("医生密码为空，跳过: username={}, employeeNumber={}", 
                    doctor.getUsername(), doctor.getEmployeeNumber());
            }
            doctor.setCreateTime(now);
            doctor.setUpdateTime(now);
            
            // 如果没有设置权限等级，默认为2（普通医生）
            if (doctor.getLevel() == null) {
                doctor.setLevel(2);
            }
        }
        
        // 批量插入
        doctorMapper.batchInsert(doctors);
        
        log.info("批量注册医生成功，数量: {}", doctors.size());
        return doctors.size();
    }

    @Override
    public List<Doctor> findAll() {
        log.info("查询所有医生");
        List<Doctor> doctors = doctorMapper.findAll();
        // 不返回密码
        doctors.forEach(doctor -> doctor.setPassword(null));
        return doctors;
    }
}
