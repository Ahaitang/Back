package org.hospital.neuroimmune.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.hospital.neuroimmune.entity.DoctorRole;
import org.hospital.neuroimmune.mapper.DoctorRoleMapper;
import org.hospital.neuroimmune.service.DoctorRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class DoctorRoleServiceImpl implements DoctorRoleService {
    @Autowired
    private DoctorRoleMapper doctorRoleMapper;

    @Override
    public List<String> getRoleCodesByDoctorId(Long doctorId) {
        return doctorRoleMapper.findRoleCodesByDoctorId(doctorId);
    }

    @Override
    public void addRoleToDoctor(Long doctorId, String roleCode) {
        DoctorRole role = new DoctorRole();
        role.setDoctorId(doctorId);
        role.setRoleCode(roleCode);
        role.setIsActive(1);
        doctorRoleMapper.insert(role);
    }

    @Override
    public void removeRoleFromDoctor(Long doctorId, String roleCode) {
        QueryWrapper<DoctorRole> wrapper = new QueryWrapper<>();
        wrapper.eq("doctor_id", doctorId).eq("role_code", roleCode);
        doctorRoleMapper.delete(wrapper);
    }

    @Override
    @Transactional
    public void setDoctorRoles(Long doctorId, List<String> roleCodes) {
        QueryWrapper<DoctorRole> wrapper = new QueryWrapper<>();
        wrapper.eq("doctor_id", doctorId);
        doctorRoleMapper.delete(wrapper);
        for (String roleCode : roleCodes) {
            DoctorRole role = new DoctorRole();
            role.setDoctorId(doctorId);
            role.setRoleCode(roleCode);
            role.setIsActive(1);
            doctorRoleMapper.insert(role);
        }
    }

    @Override
    public boolean hasRole(Long doctorId, String roleCode) {
        return getRoleCodesByDoctorId(doctorId).contains(roleCode);
    }

    @Override
    @Transactional
    public void deactivateRolesByDoctorId(Long doctorId) {
        // 将医生的所有角色设置为无效
        LambdaUpdateWrapper<DoctorRole> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DoctorRole::getDoctorId, doctorId)
                     .eq(DoctorRole::getIsActive, 1)
                     .set(DoctorRole::getIsActive, 0);
        doctorRoleMapper.update(null, updateWrapper);
    }
}