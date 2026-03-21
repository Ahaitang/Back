package org.hospital.neuroimmune.controller;

import org.hospital.neuroimmune.common.Result;
import org.hospital.neuroimmune.dto.PageRequest;
import org.hospital.neuroimmune.dto.PageResult;
import org.hospital.neuroimmune.entity.DiseaseEpisode;
import org.hospital.neuroimmune.service.DiseaseEpisodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/episodes")
@CrossOrigin
public class DiseaseEpisodeController {

    @Autowired
    private DiseaseEpisodeService diseaseEpisodeService;

    /**
     * 获取发作记录列表
     */
    @GetMapping
    public Result<PageResult<DiseaseEpisode>> list(
            PageRequest request,
            @RequestParam(required = false) Long patientId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        // 患者只能查看自己的发作记录
        if ("patient".equals(role) && userId != null) {
            request.setPatientId(userId);
        } else if (patientId != null) {
            request.setPatientId(patientId);
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
    public Result<Void> save(@RequestBody DiseaseEpisode episode,
                              @RequestHeader(value = "X-User-Role", required = false) String role,
                              @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        // 患者只能添加自己的发作记录
        if ("patient".equals(role) && userId != null) {
            episode.setPatientId(userId);
        }
        diseaseEpisodeService.save(episode);
        return Result.success();
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
}