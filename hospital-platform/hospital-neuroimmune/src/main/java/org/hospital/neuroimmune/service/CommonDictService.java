package org.hospital.neuroimmune.service;

import org.hospital.neuroimmune.entity.CommonDict;
import java.util.List;

public interface CommonDictService {
    List<CommonDict> getByType(String dictType);
    List<CommonDict> getAll();
    CommonDict getById(Long id);
    void save(CommonDict dict);
    void delete(Long id);
    void toggleActive(Long id, boolean active);
}