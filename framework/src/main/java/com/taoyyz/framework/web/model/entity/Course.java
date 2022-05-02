package com.taoyyz.framework.web.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/8 16:06
 */
@Data
@Accessors(chain = true)
public class Course {
    @TableId
    private Long courseId;
    private String courseName;
    private Long creatorId;
    private String credit;
    private String courseTime;
    private String courseLocation;
    private Integer selectedCount;
    private Integer maxCount;
    private String examDateTime;
    private String examLocation;
    private Integer type;
    @TableLogic
    private Integer isDel;
}
