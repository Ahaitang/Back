package org.hospital.qmg.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.hospital.qmg.entity.Patient;
import java.util.List;

/**
 * 患者Mapper接口
 */
@Mapper
public interface PatientMapper {
    /**
     * 查询所有患者列表
     */
    List<Patient> findAll();

    /**
     * 管理员可见的患者列表（排除超级管理员创建的患者）
     */
    List<Patient> findAllVisibleToAdmin();

    /**
     * 根据ID查询患者
     */
    Patient findById(Integer id);

    /**
     * 根据住院号查询患者
     */
    Patient findByAdmissionNumber(String admissionNumber);

    /**
     * 根据姓名或住院号搜索患者
     */
    List<Patient> searchByNameOrAdmissionNumber(String keyword);

    /**
     * 根据姓名或住院号搜索（管理员可见：排除超级管理员创建的患者）
     */
    List<Patient> searchByNameOrAdmissionNumberVisibleToAdmin(String keyword);

    /**
     * 根据医生ID查询患者列表
     */
    List<Patient> findByDoctorId(Integer doctorId);
    
    /**
     * 根据医生ID和关键词搜索患者
     */
    List<Patient> searchByDoctorIdAndKeyword(Integer doctorId, String keyword);

    /**
     * 新增患者
     */
    void insert(Patient patient);

    /**
     * 更新患者信息
     */
    void update(Patient patient);

    /**
     * 删除患者
     */
    void deleteById(Integer id);
}
