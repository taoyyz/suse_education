package com.taoyyz.framework.web.model.VO;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/5/4 6:04
 */
@Data
@Accessors(chain = true)
public class DepartmentVO {
    private Long departmentId;
    private String departmentName;
    private Long majorCount;
    private Long teacherCount;
}
