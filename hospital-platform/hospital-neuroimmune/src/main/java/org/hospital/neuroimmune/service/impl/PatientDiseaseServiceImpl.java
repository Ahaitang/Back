package org.hospital.neuroimmune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.hospital.neuroimmune.entity.PatientDisease;
import org.hospital.neuroimmune.mapper.PatientDiseaseMapper;
import org.hospital.neuroimmune.service.PatientDiseaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientDiseaseServiceImpl implements PatientDiseaseService {

    private final PatientDiseaseMapper patientDiseaseMapper;

    @Override
    public List<String> getDiseaseCodesByPatientId(Long patientId) {
        return patientDiseaseMapper.findDiseaseCodesByPatientId(patientId);
    }

    @Override
    @Transactional
    public void setDiseasesForPatient(Long patientId, List<String> diseaseCodes) {
        // 先删除旧的关联
        patientDiseaseMapper.delete(new LambdaQueryWrapper<PatientDisease>()
                .eq(PatientDisease::getPatientId, patientId));
        // 再插入新的关联
        if (diseaseCodes != null) {
            for (String code : diseaseCodes) {
                if (code != null && !code.trim().isEmpty()) {
                    PatientDisease pd = new PatientDisease();
                    pd.setPatientId(patientId);
                    pd.setDiseaseCode(code.trim());
                    patientDiseaseMapper.insert(pd);
                }
            }
        }
    }

    @Override
    public List<Long> getPatientIdsByDiseaseCode(String diseaseCode) {
        return patientDiseaseMapper.findPatientIdsByDiseaseCode(diseaseCode);
    }

    @Override
    public List<Long> getPatientIdsByDiseaseCodes(List<String> diseaseCodes) {
        if (diseaseCodes == null || diseaseCodes.isEmpty()) {
            return List.of();
        }
        // 将 List 转为带单引号的逗号分隔字符串，用于 IN 查询
        String codesStr = diseaseCodes.stream()
                .map(code -> "'" + code.replace("'", "''") + "'")
                .collect(Collectors.joining(","));
        return patientDiseaseMapper.findPatientIdsByDiseaseCodes(codesStr);
    }
}
