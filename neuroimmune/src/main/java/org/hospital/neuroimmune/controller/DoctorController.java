package org.hospital.neuroimmune.controller;

import org.hospital.neuroimmune.common.Result;
import org.hospital.neuroimmune.dto.PageRequest;
import org.hospital.neuroimmune.dto.PageResult;
import org.hospital.neuroimmune.entity.Doctor;
import org.hospital.neuroimmune.service.DoctorService;
import org.hospital.neuroimmune.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PatientService patientService;

    @GetMapping
    public Result<PageResult<Doctor>> list(PageRequest request) {
        return Result.success(doctorService.getList(request));
    }

    @GetMapping("/all")
    public Result<List<Doctor>> all() {
        PageRequest request = new PageRequest();
        request.setPageSize(10000); // 使用合理的大数值
        return Result.success(doctorService.getList(request).getList());
    }

    @GetMapping("/{id}")
    public Result<Doctor> getById(@PathVariable Long id) {
        return Result.success(doctorService.getById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody Doctor doctor) {
        doctorService.save(doctor);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Doctor doctor) {
        doctor.setId(id);
        doctorService.save(doctor);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long patientCount = patientService.countByDoctorId(id);
        if (patientCount > 0) {
            return Result.error("该医生下有 " + patientCount + " 名患者，无法删除");
        }
        doctorService.delete(id);
        return Result.success();
    }
}