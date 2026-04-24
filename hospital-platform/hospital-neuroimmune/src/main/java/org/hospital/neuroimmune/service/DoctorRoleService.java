package org.hospital.neuroimmune.service;
import java.util.List;

public interface DoctorRoleService {
    List<String> getRoleCodesByDoctorId(Long doctorId);
    void addRoleToDoctor(Long doctorId, String roleCode);
    void removeRoleFromDoctor(Long doctorId, String roleCode);
    void setDoctorRoles(Long doctorId, List<String> roleCodes);
    boolean hasRole(Long doctorId, String roleCode);
}