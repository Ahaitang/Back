package org.hospital.qmg.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.hospital.qmg.entity.Doctor;

/**
 * 医生Mapper接口
 */
@Mapper
public interface QmgDoctorMapper {
    /**
     * 根据用户名查询医生
     */
    Doctor findByUsername(String username);

    /**
     * 根据工号查询医生
     */
    Doctor findByEmployeeNumber(String employeeNumber);

    /**
     * 根据ID查询医生
     */
    Doctor findById(Integer id);

    /**
     * 新增医生
     */
    void insert(Doctor doctor);

    /**
     * 更新医生信息
     */
    void update(Doctor doctor);

    /**
     * 批量插入医生
     */
    void batchInsert(java.util.List<Doctor> doctors);

    /**
     * 查询所有医生
     */
    java.util.List<Doctor> findAll();
}
