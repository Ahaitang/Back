package org.hospital.qmg.service;

import org.hospital.qmg.entity.Patient;
import org.hospital.qmg.pojo.BatchImportResult;

import java.util.List;

/**
 * 患者Service接口
 */
public interface PatientService {
    /**
     * 查询所有患者列表（根据权限过滤）
     * @param currentDoctorId 当前登录医生ID，如果为null或权限等级为0或1，则返回所有患者
     * @param currentUserLevel 当前用户权限等级：0=超级管理员，1=管理员，2=普通医生
     */
    List<Patient> findAll(Integer currentDoctorId, Integer currentUserLevel);

    /**
     * 根据ID查询患者（根据权限过滤）
     * @param id 患者ID
     * @param currentDoctorId 当前登录医生ID，如果为null或权限等级为0或1，则可以查看所有患者
     * @param currentUserLevel 当前用户权限等级
     */
    Patient findById(Integer id, Integer currentDoctorId, Integer currentUserLevel);

    /**
     * 根据住院号查询患者
     */
    Patient findByAdmissionNumber(String admissionNumber);

    /**
     * 仅按医生ID查询其关联的患者（用于 App 端 scope=mine）
     */
    List<Patient> findByDoctorIdOnly(Integer doctorId);

    /**
     * 仅按医生ID和关键词搜索（用于 App 端 scope=mine）
     */
    List<Patient> searchByDoctorIdAndKeywordOnly(Integer doctorId, String keyword);

    /**
     * 为患者关联医生（建立 patient_doctor）
     */
    void linkDoctor(Integer patientId, Integer doctorId);

    /**
     * 根据姓名或住院号搜索患者（根据权限过滤）
     * @param keyword 搜索关键词
     * @param currentDoctorId 当前登录医生ID，如果为null或权限等级为0或1，则可以查看所有患者
     * @param currentUserLevel 当前用户权限等级
     */
    List<Patient> searchByNameOrAdmissionNumber(String keyword, Integer currentDoctorId, Integer currentUserLevel);

    /**
     * 新增患者
     */
    void save(Patient patient);

    /**
     * 更新患者信息
     */
    void update(Patient patient);

    /**
     * 删除患者
     */
    void deleteById(Integer id);

    /**
     * 批量导入患者
     * @param patients 患者列表
     * @param currentDoctorId 当前操作医生ID，不为空时为导入的患者建立与该医生的关联
     * @return 导入结果（成功数、失败数、失败明细）
     */
    BatchImportResult batchImport(List<Patient> patients, Integer currentDoctorId);
}
