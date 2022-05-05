package com.taoyyz.framework.web.model.DTO;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/5/4 2:39
 */
@Data
@Accessors(chain = true)
public class CourseDTO {
    private Long courseId;
    private String courseName;
}
