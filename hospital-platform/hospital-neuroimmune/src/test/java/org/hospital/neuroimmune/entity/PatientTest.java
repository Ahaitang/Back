package org.hospital.neuroimmune.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Patient 实体类测试（Neuroimmune 模块）
 */
class PatientTest {

    @Test
    @DisplayName("测试创建患者实体")
    void testCreatePatient() {
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setName("李四");
        patient.setGender("男");
        patient.setPhone("13900139000");
        patient.setDoctorId(1L);
        patient.setDoctorName("王医生");

        assertEquals(1L, patient.getId());
        assertEquals("李四", patient.getName());
        assertEquals("男", patient.getGender());
        assertEquals("13900139000", patient.getPhone());
    }

    @Test
    @DisplayName("测试疾病类型列表")
    void testDiseaseTypes() {
        Patient msPatient = new Patient();
        msPatient.setDiseaseTypes(List.of("MS"));
        assertEquals(List.of("MS"), msPatient.getDiseaseTypes());

        Patient nmosdPatient = new Patient();
        nmosdPatient.setDiseaseTypes(List.of("NMOSD"));
        assertEquals(List.of("NMOSD"), nmosdPatient.getDiseaseTypes());

        Patient mgPatient = new Patient();
        mgPatient.setDiseaseTypes(List.of("MG"));
        assertEquals(List.of("MG"), mgPatient.getDiseaseTypes());

        // 多疾病类型患者
        Patient multiPatient = new Patient();
        multiPatient.setDiseaseTypes(Arrays.asList("MS", "NMOSD"));
        assertEquals(2, multiPatient.getDiseaseTypes().size());
    }

    @Test
    @DisplayName("测试随访状态")
    void testFollowUpStatus() {
        Patient patient = new Patient();
        patient.setHasFollowUp(true);
        assertTrue(patient.getHasFollowUp());

        patient.setHasFollowUp(false);
        assertFalse(patient.getHasFollowUp());
    }

    @Test
    @DisplayName("测试实名认证状态")
    void testRealAuthStatus() {
        Patient patient = new Patient();
        patient.setIsRealAuth(true);
        assertTrue(patient.getIsRealAuth());

        patient.setIsRealAuth(false);
        assertFalse(patient.getIsRealAuth());
    }
}