package com.taoyyz.framework.web.model.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/8 16:06
 */
@Data
@Accessors(chain = true)
public class CourseVO {
    @TableId
    private Long courseId;
    private String courseName;
    private Long creatorId;
    private String creatorName;
    private String credit;
    private String courseTime;
    private String courseLocation;
    private Integer selectedCount;
    private Integer maxCount;
    private String examDateTime;
    private String examLocation;
    private String dailyScore;
    private String examScore;
    private String totalScore;
    private String type;
}
