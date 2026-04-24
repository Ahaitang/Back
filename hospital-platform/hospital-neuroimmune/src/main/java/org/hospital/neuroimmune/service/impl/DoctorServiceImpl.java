package org.hospital.neuroimmune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hospital.common.model.LoginRequest;
import org.hospital.common.model.PageRequest;
import org.hospital.common.model.PageResult;
import org.hospital.neuroimmune.entity.Doctor;
import org.hospital.neuroimmune.mapper.NeuroimmuneDoctorMapper;
import org.hospital.neuroimmune.service.DoctorRoleService;
import org.hospital.neuroimmune.service.DoctorService;
import org.hospital.neuroimmune.service.PatientDoctorRelationService;
import org.hospital.common.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("neuroimmuneDoctorService")
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private NeuroimmuneDoctorMapper doctorMapper;

    @Autowired
    private PatientDoctorRelationService relationService;

    @Autowired
    private DoctorRoleService doctorRoleService;

    @Override
    public PageResult<Doctor> getList(PageRequest request) {
        Page<Doctor> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        // 只查询有效数据
        wrapper.and(w -> w.eq(Doctor::getIsDeleted, 0).or().isNull(Doctor::getIsDeleted));
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
            Long count = relationService.countPatientsByDoctor(doctor.getId());
            doctor.setPatientCount(count.intValue());
        });

        return new PageResult<>(result.getRecords(), result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public PageResult<Doctor> getDoctorList(PageRequest request) {
        // 使用 DoctorRoleMapper 查询有 DOCTOR 角色的医生
        List<Doctor> doctors = doctorMapper.selectDoctorsWithRoles();

        // 关键词过滤
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            doctors = doctors.stream()
                    .filter(d -> d.getName().contains(request.getKeyword()) ||
                            (d.getPhone() != null && d.getPhone().contains(request.getKeyword())))
                    .toList();
        }

        // 部门过滤
        if (request.getDepartment() != null && !request.getDepartment().isEmpty()) {
            doctors = doctors.stream()
                    .filter(d -> d.getDepartment() != null && d.getDepartment().equals(request.getDepartment()))
                    .toList();
        }

        // 设置每个医生的患者数量
        doctors.forEach(doctor -> {
            Long count = relationService.countPatientsByDoctor(doctor.getId());
            doctor.setPatientCount(count.intValue());
        });

        // 手动分页
        int total = doctors.size();
        int start = (request.getPageNum() - 1) * request.getPageSize();
        int end = Math.min(start + request.getPageSize(), total);
        List<Doctor> pageData = doctors.subList(start, end);

        return new PageResult<>(pageData, (long) total, request.getPageNum(), request.getPageSize());
    }

    @Override
    public PageResult<Doctor> getAdminList(PageRequest request) {
        // 使用 DoctorRoleMapper 查询有 ADMIN 角色的医生
        List<Doctor> admins = doctorMapper.selectAdminsWithRoles();

        // 关键词过滤
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            admins = admins.stream()
                    .filter(d -> d.getName().contains(request.getKeyword()) ||
                            (d.getPhone() != null && d.getPhone().contains(request.getKeyword())))
                    .toList();
        }

        // 设置每个管理员的患者数量
        admins.forEach(doctor -> {
            Long count = relationService.countPatientsByDoctor(doctor.getId());
            doctor.setPatientCount(count.intValue());
        });

        // 手动分页
        int total = admins.size();
        int start = (request.getPageNum() - 1) * request.getPageSize();
        int end = Math.min(start + request.getPageSize(), total);
        List<Doctor> pageData = admins.subList(start, end);

        return new PageResult<>(pageData, (long) total, request.getPageNum(), request.getPageSize());
    }

    @Override
    public Doctor getById(Long id) {
        Doctor doctor = doctorMapper.selectById(id);
        if (doctor != null) {
            Long count = relationService.countPatientsByDoctor(id);
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
            // 默认添加 DOCTOR 角色
            doctorRoleService.addRoleToDoctor(doctor.getId(), "DOCTOR");
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
    public void updateRoleAndLevel(Long id, String role, Integer level) {
        // role 参数已废弃，使用 DoctorRoleService 管理角色
        if (level != null) {
            LambdaUpdateWrapper<Doctor> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Doctor::getId, id).set(Doctor::getLevel, level);
            doctorMapper.update(null, updateWrapper);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 逻辑删除医生
        LambdaUpdateWrapper<Doctor> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Doctor::getId, id).set(Doctor::getIsDeleted, 1);
        doctorMapper.update(null, updateWrapper);

        // 同步解绑医生的所有患者关系
        relationService.unbindAllByDoctorId(id);

        // 同时禁用医生的角色
        doctorRoleService.deactivateRolesByDoctorId(id);
    }

    @Override
    public void batchInsert(List<Doctor> doctors) {
        for (Doctor doctor : doctors) {
            if (doctor.getPassword() != null && !doctor.getPassword().startsWith("$2")) {
                doctor.setPassword(PasswordUtil.encode(doctor.getPassword()));
            }
            doctorMapper.insert(doctor);
            // 默认添加 DOCTOR 角色
            doctorRoleService.addRoleToDoctor(doctor.getId(), "DOCTOR");
        }
    }

    @Override
    public Doctor getByPhone(String phone) {
        return doctorMapper.selectByPhone(phone);
    }
}