package com.taoyyz.framework.web.model.DTO;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/5/4 3:37
 */
@Data
@Accessors(chain = true)
public class CourseScoreDTO {
    /**
     * 位于course_select表的id
     */
    private Long tbId;

    private String courseName;

    private String studentName;

    private String dailyScore;

    private String examScore;

    private String totalScore;

    /**
     * 课程性质：0选修，1必修
     */
    private Integer type;
}
