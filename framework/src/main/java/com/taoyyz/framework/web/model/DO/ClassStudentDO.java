package com.taoyyz.framework.web.model.DO;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/5/4 3:01
 */
@Data
@Accessors(chain = true)
public class ClassStudentDO {
    private Long tbId;
    private Long classId;
    private Long studentId;
}
