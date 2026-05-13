package org.hospital.neuroimmune.controller;

import org.hospital.common.model.Result;
import org.hospital.common.model.PageRequest;
import org.hospital.common.model.PageResult;
import org.hospital.common.security.SecurityContextHelper;
import org.hospital.common.security.UserInfo;
import org.hospital.neuroimmune.entity.DiseaseEpisode;
import org.hospital.neuroimmune.service.DiseaseEpisodeService;
import org.hospital.neuroimmune.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/neuroimmune/episodes")
@CrossOrigin
public class DiseaseEpisodeController {

    @Autowired
    private DiseaseEpisodeService diseaseEpisodeService;

    @Autowired
    private PermissionService permissionService;

    /**
     * 获取发作记录列表
     * 支持参数：
     * - patientId: 按患者筛选
     * - keyword: 关键词搜索（主诉/诊断）
     */
    @GetMapping
    public Result<PageResult<DiseaseEpisode>> list(@ModelAttribute PageRequest request) {

        UserInfo userInfo = getCurrentUser();
        // 患者只能查看自己的发作记录
        if ("patient".equals(userInfo.getRole())) {
            request.setPatientId(userInfo.getUserId());
        } else if ("doctor".equals(userInfo.getRole())) {
            request.setDoctorId(userInfo.getUserId());
        }

        return Result.success(diseaseEpisodeService.getList(request));
    }

    /**
     * 获取患者的所有发作记录
     */
    @GetMapping("/patient/{patientId}")
    public Result<List<DiseaseEpisode>> listByPatient(@PathVariable Long patientId) {
        String error = checkPatientAccess(patientId);
        if (error != null) return Result.error(403, error);
        return Result.success(diseaseEpisodeService.getByPatientId(patientId));
    }

    /**
     * 获取发作记录详情
     */
    @GetMapping("/{id}")
    public Result<DiseaseEpisode> getById(@PathVariable Long id) {
        DiseaseEpisode episode = diseaseEpisodeService.getById(id);
        String error = checkPatientAccess(episode != null ? episode.getPatientId() : null);
        if (error != null) return Result.error(403, error);
        return Result.success(episode);
    }

    /**
     * 新增/更新发作记录
     */
    @PostMapping
    public Result<Long> save(@RequestBody DiseaseEpisode episode) {
        UserInfo userInfo = getCurrentUser();
        // 患者只能添加自己的发作记录
        if ("patient".equals(userInfo.getRole())) {
            episode.setPatientId(userInfo.getUserId());
        }
        String error = checkPatientAccess(episode.getPatientId());
        if (error != null) return Result.error(403, error);
        diseaseEpisodeService.save(episode);
        return Result.success(episode.getId());
    }

    /**
     * 更新发作记录
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody DiseaseEpisode episode) {
        DiseaseEpisode existing = diseaseEpisodeService.getById(id);
        String error = checkPatientAccess(existing != null ? existing.getPatientId() : episode.getPatientId());
        if (error != null) return Result.error(403, error);
        episode.setId(id);
        diseaseEpisodeService.save(episode);
        return Result.success();
    }

    /**
     * 删除发作记录
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        DiseaseEpisode existing = diseaseEpisodeService.getById(id);
        String error = checkPatientAccess(existing != null ? existing.getPatientId() : null);
        if (error != null) return Result.error(403, error);
        diseaseEpisodeService.delete(id);
        return Result.success();
    }

    /**
     * 获取患者发作次数
     */
    @GetMapping("/count/{patientId}")
    public Result<Integer> countByPatient(@PathVariable Long patientId) {
        String error = checkPatientAccess(patientId);
        if (error != null) return Result.error(403, error);
        return Result.success(diseaseEpisodeService.countByPatientId(patientId));
    }

    private String checkPatientAccess(Long patientId) {
        if (patientId == null) {
            return "记录不存在";
        }
        return permissionService.checkPatientAccessPermission(
                SecurityContextHelper.getCurrentUserId(),
                SecurityContextHelper.getCurrentRole(),
                patientId);
    }

    private UserInfo getCurrentUser() {
        return (UserInfo) SecurityContextHolder.getContext().getAuthentication().getDetails();
    }
}
