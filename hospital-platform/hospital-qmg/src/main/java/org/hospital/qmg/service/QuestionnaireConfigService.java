package org.hospital.qmg.service;

import org.hospital.qmg.entity.QuestionnaireItem;
import java.util.List;

/**
 * 问卷配置Service接口
 */
public interface QuestionnaireConfigService {
    
    /**
     * 获取所有问卷项目（包含选项）
     */
    List<QuestionnaireItem> getAllItems();
    
    /**
     * 根据分类获取问卷项目
     */
    List<QuestionnaireItem> getItemsByCategory(String category);
    
    /**
     * 根据键名获取问卷项目
     */
    QuestionnaireItem getItemByKey(String key);
    
    /**
     * 保存或更新问卷项目（包含选项）
     */
    void saveOrUpdateItem(QuestionnaireItem item);
    
    /**
     * 删除问卷项目（会级联删除选项）
     */
    void deleteItem(Integer itemId);
    
    /**
     * 批量保存或更新问卷项目
     */
    void batchSaveOrUpdateItems(List<QuestionnaireItem> items);
}
