package org.hospital.neuroimmune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.hospital.neuroimmune.entity.CommonDict;
import org.hospital.neuroimmune.mapper.CommonDictMapper;
import org.hospital.neuroimmune.service.CommonDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommonDictServiceImpl implements CommonDictService {

    @Autowired
    private CommonDictMapper dictMapper;

    @Override
    public List<CommonDict> getByType(String dictType) {
        return dictMapper.selectByType(dictType);
    }

    @Override
    public List<CommonDict> getAll() {
        LambdaQueryWrapper<CommonDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(CommonDict::getDictType)
               .orderByAsc(CommonDict::getSortOrder);
        return dictMapper.selectList(wrapper);
    }

    @Override
    public CommonDict getById(Long id) {
        return dictMapper.selectById(id);
    }

    @Override
    public void save(CommonDict dict) {
        LocalDateTime now = LocalDateTime.now();
        if (dict.getId() == null) {
            dict.setIsActive(1);
            dict.setCreateTime(now);
            dict.setUpdateTime(now);
            if (dict.getSortOrder() == null) {
                dict.setSortOrder(0);
            }
            dictMapper.insert(dict);
        } else {
            dict.setUpdateTime(now);
            dictMapper.updateById(dict);
        }
    }

    @Override
    public void delete(Long id) {
        dictMapper.deleteById(id);
    }

    @Override
    public void toggleActive(Long id, boolean active) {
        LambdaUpdateWrapper<CommonDict> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CommonDict::getId, id)
               .set(CommonDict::getIsActive, active ? 1 : 0)
               .set(CommonDict::getUpdateTime, LocalDateTime.now());
        dictMapper.update(null, wrapper);
    }
}