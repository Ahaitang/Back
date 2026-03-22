package org.hospital.neuroimmune.dto;

import lombok.Data;
import java.util.List;

/**
 * 日程数据传输对象
 */
@Data
public class ScheduleDTO {

    private List<ScheduleItem> visit;      // 就诊计划
    private List<ScheduleItem> follow;     // 随访计划
    private List<ScheduleItem> medication; // 用药建议

    @Data
    public static class ScheduleItem {
        private Long id;
        private String time;           // 时间范围，如 "09:00-10:00"
        private String who;            // 相关人员（医生/患者姓名）
        private String date;           // 开始日期
        private String endDate;        // 结束日期（用药建议用）
        private String type;           // 类型
        private String status;         // 状态
        private String content;        // 内容描述
    }
}