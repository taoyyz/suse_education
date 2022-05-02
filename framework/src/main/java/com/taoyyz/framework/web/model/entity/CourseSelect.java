package com.taoyyz.framework.web.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/16 22:50
 */
@Data
@Accessors(chain = true)
public class CourseSelect {
    @TableId
    private Long tbId;
    private Long courseId;
    private Long userId;
    private String dailyScore;
    private String examScore;
    private String totalScore;
    /**
     * 课程类型：必修或选修
     */
    private String type;
}
