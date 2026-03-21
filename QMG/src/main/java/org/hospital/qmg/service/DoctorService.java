package org.hospital.qmg.service;

import org.hospital.qmg.entity.Doctor;

/**
 * 医生Service接口
 */
public interface DoctorService {
    /**
     * 根据用户名查询医生
     */
    Doctor findByUsername(String username);

    /**
     * 根据工号查询医生
     */
    Doctor findByEmployeeNumber(String employeeNumber);

    /**
     * 医生登录验证
     */
    Doctor login(String username, String password);

    /**
     * 新增医生
     */
    void save(Doctor doctor);

    /**
     * 更新医生信息
     */
    void update(Doctor doctor);

    /**
     * 根据ID查询医生
     */
    Doctor findById(Integer id);

    /**
     * 批量注册医生
     */
    int batchRegister(java.util.List<Doctor> doctors);

    /**
     * 查询所有医生
     */
    java.util.List<Doctor> findAll();
}
