package org.hospital.qmg.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Doctor 实体类测试
 */
class DoctorTest {

    @Test
    @DisplayName("测试创建医生实体")
    void testCreateDoctor() {
        Doctor doctor = new Doctor();
        doctor.setId(1);
        doctor.setEmployeeNumber("EMP001");
        doctor.setUsername("doctor1");
        doctor.setPassword("encoded_password");
        doctor.setLevel(2);
        doctor.setCreateTime(LocalDateTime.now());
        doctor.setUpdateTime(LocalDateTime.now());

        assertEquals(1, doctor.getId());
        assertEquals("EMP001", doctor.getEmployeeNumber());
        assertEquals("doctor1", doctor.getUsername());
        assertEquals(2, doctor.getLevel());
    }

    @Test
    @DisplayName("测试权限等级")
    void testLevelField() {
        Doctor admin = new Doctor();
        admin.setLevel(0);
        assertEquals(0, admin.getLevel());

        Doctor manager = new Doctor();
        manager.setLevel(1);
        assertEquals(1, manager.getLevel());

        Doctor normalDoctor = new Doctor();
        normalDoctor.setLevel(2);
        assertEquals(2, normalDoctor.getLevel());
    }
}