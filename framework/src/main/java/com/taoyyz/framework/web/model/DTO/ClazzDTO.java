package com.taoyyz.framework.web.model.DTO;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/5/4 2:56
 */
@Data
@Accessors(chain = true)
public class ClazzDTO {
    private Long classId;
    private String className;
}
