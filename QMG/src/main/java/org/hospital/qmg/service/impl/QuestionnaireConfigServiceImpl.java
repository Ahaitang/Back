package org.hospital.qmg.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.hospital.qmg.entity.QuestionnaireItem;
import org.hospital.qmg.entity.QuestionnaireOption;
import org.hospital.qmg.mapper.QuestionnaireItemMapper;
import org.hospital.qmg.mapper.QuestionnaireOptionMapper;
import org.hospital.qmg.service.QuestionnaireConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 问卷配置Service实现类
 */
@Slf4j
@Service
public class QuestionnaireConfigServiceImpl implements QuestionnaireConfigService {

    @Autowired
    private QuestionnaireItemMapper itemMapper;
    
    @Autowired
    private QuestionnaireOptionMapper optionMapper;

    @Override
    public List<QuestionnaireItem> getAllItems() {
        List<QuestionnaireItem> items = itemMapper.findAll();
        // 为每个项目加载选项
        for (QuestionnaireItem item : items) {
            List<QuestionnaireOption> options = optionMapper.findByItemKey(item.getKey());
            item.setOptions(options);
        }
        return items;
    }

    @Override
    public List<QuestionnaireItem> getItemsByCategory(String category) {
        List<QuestionnaireItem> items = itemMapper.findByCategory(category);
        // 为每个项目加载选项
        for (QuestionnaireItem item : items) {
            List<QuestionnaireOption> options = optionMapper.findByItemKey(item.getKey());
            item.setOptions(options);
        }
        return items;
    }

    @Override
    public QuestionnaireItem getItemByKey(String key) {
        QuestionnaireItem item = itemMapper.findByKey(key);
        if (item != null) {
            List<QuestionnaireOption> options = optionMapper.findByItemKey(key);
            item.setOptions(options);
        }
        return item;
    }

    @Override
    @Transactional
    public void saveOrUpdateItem(QuestionnaireItem item) {
        LocalDateTime now = LocalDateTime.now();
        
        if (item.getId() == null) {
            // 新增
            item.setCreateTime(now);
            item.setUpdateTime(now);
            itemMapper.insert(item);
            log.info("新增问卷项目: key={}, name={}", item.getKey(), item.getName());
        } else {
            // 更新
            item.setUpdateTime(now);
            itemMapper.update(item);
            log.info("更新问卷项目: id={}, key={}, name={}", item.getId(), item.getKey(), item.getName());
        }
        
        // 删除旧选项
        optionMapper.deleteByItemKey(item.getKey());
        
        // 保存新选项
        if (item.getOptions() != null && !item.getOptions().isEmpty()) {
            for (QuestionnaireOption option : item.getOptions()) {
                option.setItemKey(item.getKey());
                option.setCreateTime(now);
                option.setUpdateTime(now);
            }
            optionMapper.batchInsert(item.getOptions());
            log.info("保存问卷选项: itemKey={}, optionCount={}", item.getKey(), item.getOptions().size());
        }
    }

    @Override
    @Transactional
    public void deleteItem(Integer itemId) {
        QuestionnaireItem item = itemMapper.findById(itemId);
        if (item != null) {
            // 删除选项（外键级联删除）
            optionMapper.deleteByItemKey(item.getKey());
            // 删除项目
            itemMapper.deleteById(itemId);
            log.info("删除问卷项目: id={}, key={}", itemId, item.getKey());
        }
    }

    @Override
    @Transactional
    public void batchSaveOrUpdateItems(List<QuestionnaireItem> items) {
        for (QuestionnaireItem item : items) {
            saveOrUpdateItem(item);
        }
        log.info("批量保存问卷项目: count={}", items.size());
    }
}
