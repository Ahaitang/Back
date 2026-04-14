package org.hospital.neuroimmune.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.hospital.neuroimmune.entity.DiseaseEpisode;
import org.hospital.neuroimmune.dto.PageRequest;
import java.util.List;

@Mapper
public interface DiseaseEpisodeMapper extends BaseMapper<DiseaseEpisode> {

    List<DiseaseEpisode> selectList(PageRequest request);

    Long selectCount(PageRequest request);

    List<DiseaseEpisode> selectByPatientId(Long patientId);

    Integer countByPatientId(Long patientId);
}