package org.hospital.neuroimmune.controller;

import org.hospital.common.model.Result;
import org.hospital.common.model.PageRequest;
import org.hospital.common.model.PageResult;
import org.hospital.common.security.UserInfo;
import org.hospital.neuroimmune.entity.DiseaseEpisode;
import org.hospital.neuroimmune.service.DiseaseEpisodeService;
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
        }

        return Result.success(diseaseEpisodeService.getList(request));
    }

    /**
     * 获取患者的所有发作记录
     */
    @GetMapping("/patient/{patientId}")
    public Result<List<DiseaseEpisode>> listByPatient(@PathVariable Long patientId) {
        return Result.success(diseaseEpisodeService.getByPatientId(patientId));
    }

    /**
     * 获取发作记录详情
     */
    @GetMapping("/{id}")
    public Result<DiseaseEpisode> getById(@PathVariable Long id) {
        return Result.success(diseaseEpisodeService.getById(id));
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
        diseaseEpisodeService.save(episode);
        return Result.success(episode.getId());
    }

    /**
     * 更新发作记录
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody DiseaseEpisode episode) {
        episode.setId(id);
        diseaseEpisodeService.save(episode);
        return Result.success();
    }

    /**
     * 删除发作记录
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        diseaseEpisodeService.delete(id);
        return Result.success();
    }

    /**
     * 获取患者发作次数
     */
    @GetMapping("/count/{patientId}")
    public Result<Integer> countByPatient(@PathVariable Long patientId) {
        return Result.success(diseaseEpisodeService.countByPatientId(patientId));
    }

    private UserInfo getCurrentUser() {
        return (UserInfo) SecurityContextHolder.getContext().getAuthentication().getDetails();
    }
}