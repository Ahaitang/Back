package org.hospital.neuroimmune.controller;

import org.hospital.common.model.Result;
import org.hospital.neuroimmune.entity.CommonDict;
import org.hospital.neuroimmune.service.CommonDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/neuroimmune/dict/common")
@CrossOrigin
public class CommonDictController {

    @Autowired
    private CommonDictService dictService;

    /**
     * 根据字典类型获取列表
     * @param dictType 字典类型：department/title/recordType/followUpType/gender/status
     */
    @GetMapping("/type/{dictType}")
    public Result<List<CommonDict>> getByType(@PathVariable String dictType) {
        return Result.success(dictService.getByType(dictType));
    }

    /**
     * 获取所有字典（管理用）
     */
    @GetMapping
    public Result<List<CommonDict>> getAll() {
        return Result.success(dictService.getAll());
    }

    /**
     * 获取单个字典项
     */
    @GetMapping("/{id}")
    public Result<CommonDict> getById(@PathVariable Long id) {
        return Result.success(dictService.getById(id));
    }

    /**
     * 新增或更新字典项
     */
    @PostMapping
    public Result<Void> save(@RequestBody CommonDict dict) {
        dictService.save(dict);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody CommonDict dict) {
        dict.setId(id);
        dictService.save(dict);
        return Result.success();
    }

    /**
     * 删除字典项
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dictService.delete(id);
        return Result.success();
    }

    /**
     * 启用/禁用字典项
     */
    @PutMapping("/{id}/active")
    public Result<Void> toggleActive(@PathVariable Long id, @RequestParam boolean active) {
        dictService.toggleActive(id, active);
        return Result.success();
    }
}