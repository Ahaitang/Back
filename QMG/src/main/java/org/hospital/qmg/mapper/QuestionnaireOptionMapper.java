package org.hospital.qmg.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.hospital.qmg.entity.QuestionnaireOption;
import java.util.List;

/**
 * 问卷选项配置Mapper接口
 */
@Mapper
public interface QuestionnaireOptionMapper {
    
    /**
     * 根据项目键名查询所有选项（按显示顺序排序）
     */
    List<QuestionnaireOption> findByItemKey(@Param("itemKey") String itemKey);
    
    /**
     * 根据ID查询选项
     */
    QuestionnaireOption findById(@Param("id") Integer id);
    
    /**
     * 新增选项
     */
    int insert(QuestionnaireOption option);
    
    /**
     * 批量新增选项
     */
    int batchInsert(@Param("options") List<QuestionnaireOption> options);
    
    /**
     * 更新选项
     */
    int update(QuestionnaireOption option);
    
    /**
     * 删除选项
     */
    int deleteById(@Param("id") Integer id);
    
    /**
     * 根据项目键名删除所有选项
     */
    int deleteByItemKey(@Param("itemKey") String itemKey);
}
