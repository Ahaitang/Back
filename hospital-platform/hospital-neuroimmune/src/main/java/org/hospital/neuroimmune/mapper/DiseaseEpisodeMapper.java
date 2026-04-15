package org.hospital.neuroimmune.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.hospital.neuroimmune.entity.DiseaseEpisode;
import java.util.List;

@Mapper
public interface DiseaseEpisodeMapper extends BaseMapper<DiseaseEpisode> {

    @Select("SELECT * FROM disease_episode WHERE patient_id = #{patientId} ORDER BY episode_date DESC, episode_number ASC")
    List<DiseaseEpisode> selectByPatientId(@Param("patientId") Long patientId);

    @Select("SELECT COUNT(*) FROM disease_episode WHERE patient_id = #{patientId}")
    Integer countByPatientId(@Param("patientId") Long patientId);
}