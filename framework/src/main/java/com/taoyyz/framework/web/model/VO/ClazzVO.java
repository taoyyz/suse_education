package com.taoyyz.framework.web.model.VO;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/5/5 0:09
 */
@Data
@Accessors(chain = true)
public class ClazzVO {
    private Long classId;
    private String className;
    private String classGrade;
    private String majorName;
    private String departmentName;
    private Long studentCount;
}
