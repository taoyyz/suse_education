package com.taoyyz.framework.web.model.request;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/5/4 21:21
 */
@Data
@Accessors(chain = true)
public class MajorRequest {
    private Long majorId;
    @NotNull
    @Size(min = 2, max = 30, message = "专业名称为2到30个字符")
    private String majorName;
    @NotNull(message = "学院/系部设置有误")
    @PositiveOrZero(message = "学院/系部设置有误")
    private Long departmentId;
}
