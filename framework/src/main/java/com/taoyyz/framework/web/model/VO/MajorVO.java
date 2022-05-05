package com.taoyyz.framework.web.model.VO;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/5/4 18:47
 */
@Data
@Accessors(chain = true)
public class MajorVO {
    private Long majorId;
    private String majorName;
    private String departmentName;
    private Long classCount;
    private Long studentCount;
}
