package org.hospital.qmg.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.hospital.qmg.entity.QuestionnaireItem;
import java.util.List;

/**
 * 问卷项目配置Mapper接口
 */
@Mapper
public interface QuestionnaireItemMapper {
    
    /**
     * 查询所有问卷项目（按显示顺序排序）
     */
    List<QuestionnaireItem> findAll();
    
    /**
     * 根据分类查询问卷项目
     */
    List<QuestionnaireItem> findByCategory(@Param("category") String category);
    
    /**
     * 根据键名查询问卷项目
     */
    QuestionnaireItem findByKey(@Param("key") String key);
    
    /**
     * 根据ID查询问卷项目
     */
    QuestionnaireItem findById(@Param("id") Integer id);
    
    /**
     * 新增问卷项目
     */
    int insert(QuestionnaireItem item);
    
    /**
     * 更新问卷项目
     */
    int update(QuestionnaireItem item);
    
    /**
     * 删除问卷项目
     */
    int deleteById(@Param("id") Integer id);
    
    /**
     * 根据键名删除问卷项目
     */
    int deleteByKey(@Param("key") String key);
}
