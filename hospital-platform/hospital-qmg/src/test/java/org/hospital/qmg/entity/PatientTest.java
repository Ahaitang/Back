package org.hospital.qmg.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Patient 实体类测试
 */
class PatientTest {

    @Test
    @DisplayName("测试创建患者实体")
    void testCreatePatient() {
        Patient patient = new Patient();
        patient.setId(1);
        patient.setName("张三");
        patient.setGender("male");
        patient.setAdmissionNumber("ADM001");
        patient.setPhone("13800138000");
        patient.setCreateTime(LocalDateTime.now());
        patient.setUpdateTime(LocalDateTime.now());

        assertEquals(1, patient.getId());
        assertEquals("张三", patient.getName());
        assertEquals("male", patient.getGender());
        assertEquals("ADM001", patient.getAdmissionNumber());
        assertEquals("13800138000", patient.getPhone());
        assertNotNull(patient.getCreateTime());
    }

    @Test
    @DisplayName("测试患者性别字段")
    void testGenderField() {
        Patient male = new Patient();
        male.setGender("male");
        assertEquals("male", male.getGender());

        Patient female = new Patient();
        female.setGender("female");
        assertEquals("female", female.getGender());
    }

    @Test
    @DisplayName("测试空值处理")
    void testNullValues() {
        Patient patient = new Patient();
        assertNull(patient.getId());
        assertNull(patient.getName());
        assertNull(patient.getGender());
    }
}