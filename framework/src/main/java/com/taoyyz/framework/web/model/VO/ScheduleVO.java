package com.taoyyz.framework.web.model.VO;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/20 23:18
 */
@Data
@Accessors(chain = true)
public class ScheduleVO {
    private String courseName;
    private String teacherName;
    private String location;
    private String time;
}
