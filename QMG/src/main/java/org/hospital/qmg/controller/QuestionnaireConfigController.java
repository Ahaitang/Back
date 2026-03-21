package org.hospital.qmg.controller;

import lombok.extern.slf4j.Slf4j;
import org.hospital.qmg.entity.QuestionnaireItem;
import org.hospital.qmg.pojo.Result;
import org.hospital.qmg.service.QuestionnaireConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 问卷配置Controller
 * 全部使用POST请求
 */
@Slf4j
@RestController
@RequestMapping("/api/questionnaireConfig")
@CrossOrigin(origins = "*")
public class QuestionnaireConfigController {

    @Autowired
    private QuestionnaireConfigService questionnaireConfigService;

    /**
     * 测试接口（用于验证服务是否正常）
     */
    @PostMapping("/test")
    public Result test() {
        log.info("问卷配置测试接口被调用");
        return Result.success("问卷配置接口正常");
    }

    /**
     * 获取所有问卷项目（包含选项）
     */
    @PostMapping("/getAllItems")
    public Result getAllItems() {
        log.info("获取所有问卷项目");
        try {
            List<QuestionnaireItem> items = questionnaireConfigService.getAllItems();
            return Result.success(items);
        } catch (Exception e) {
            log.error("获取问卷项目失败: {}", e.getMessage(), e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 根据分类获取问卷项目
     */
    @PostMapping("/getItemsByCategory")
    public Result getItemsByCategory(@RequestBody Map<String, String> params) {
        String category = params.get("category");
        log.info("根据分类获取问卷项目: category={}", category);
        try {
            List<QuestionnaireItem> items = questionnaireConfigService.getItemsByCategory(category);
            return Result.success(items);
        } catch (Exception e) {
            log.error("获取问卷项目失败: {}", e.getMessage(), e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 根据键名获取问卷项目
     */
    @PostMapping("/getItemByKey")
    public Result getItemByKey(@RequestBody Map<String, String> params) {
        String key = params.get("key");
        log.info("根据键名获取问卷项目: key={}", key);
        try {
            QuestionnaireItem item = questionnaireConfigService.getItemByKey(key);
            if (item != null) {
                return Result.success(item);
            }
            return Result.error("问卷项目不存在");
        } catch (Exception e) {
            log.error("获取问卷项目失败: {}", e.getMessage(), e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 保存或更新问卷项目（包含选项）
     */
    @PostMapping("/saveOrUpdateItem")
    public Result saveOrUpdateItem(@RequestBody QuestionnaireItem item) {
        log.info("保存或更新问卷项目: key={}, name={}", item.getKey(), item.getName());
        try {
            questionnaireConfigService.saveOrUpdateItem(item);
            return Result.success("保存成功");
        } catch (Exception e) {
            log.error("保存问卷项目失败: {}", e.getMessage(), e);
            return Result.error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 批量保存或更新问卷项目
     */
    @PostMapping("/batchSaveOrUpdateItems")
    public Result batchSaveOrUpdateItems(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<QuestionnaireItem> items = (List<QuestionnaireItem>) params.get("items");
        log.info("批量保存或更新问卷项目: count={}", items != null ? items.size() : 0);
        try {
            if (items != null && !items.isEmpty()) {
                questionnaireConfigService.batchSaveOrUpdateItems(items);
                return Result.success("批量保存成功");
            }
            return Result.error("项目列表为空");
        } catch (Exception e) {
            log.error("批量保存问卷项目失败: {}", e.getMessage(), e);
            return Result.error("批量保存失败: " + e.getMessage());
        }
    }

    /**
     * 删除问卷项目
     */
    @PostMapping("/deleteItem")
    public Result deleteItem(@RequestBody Map<String, Integer> params) {
        Integer itemId = params.get("itemId");
        log.info("删除问卷项目: itemId={}", itemId);
        try {
            questionnaireConfigService.deleteItem(itemId);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除问卷项目失败: {}", e.getMessage(), e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}
