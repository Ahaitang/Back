package org.hospital.neuroimmune.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Doctor 实体类测试（Neuroimmune 模块）
 */
class DoctorTest {

    @Test
    @DisplayName("测试创建医生实体")
    void testCreateDoctor() {
        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setName("王医生");
        doctor.setTitle("主治医师");
        doctor.setDepartment("神经内科");
        doctor.setHospital("XX医院");
        doctor.setPhone("13800138000");

        assertEquals(1L, doctor.getId());
        assertEquals("王医生", doctor.getName());
        assertEquals("主治医师", doctor.getTitle());
        assertEquals("神经内科", doctor.getDepartment());
    }

    @Test
    @DisplayName("测试职称字段")
    void testTitleField() {
        Doctor doctor = new Doctor();
        doctor.setTitle("主任医师");
        assertEquals("主任医师", doctor.getTitle());

        doctor.setTitle("副主任医师");
        assertEquals("副主任医师", doctor.getTitle());
    }

    @Test
    @DisplayName("测试患者数量字段")
    void testPatientCount() {
        Doctor doctor = new Doctor();
        doctor.setPatientCount(10);
        assertEquals(10, doctor.getPatientCount());
    }
}