package org.hospital.neuroimmune.service;

import org.hospital.neuroimmune.entity.Admin;
import org.hospital.common.model.LoginRequest;

public interface AdminService {
    Admin login(LoginRequest request);
    Admin getById(Long id);
    void updatePassword(Long id, String password);
}