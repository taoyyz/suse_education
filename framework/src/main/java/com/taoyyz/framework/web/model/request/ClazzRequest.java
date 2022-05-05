package com.taoyyz.framework.web.model.request;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/5/5 1:41
 */
@Data
@Accessors(chain = true)
public class ClazzRequest {
    private Long classId;
    @NotNull(message = "班级名称不能为空")
    @Size(min = 2, max = 30, message = "班级名称为2到30个字符")
    private String className;
    @NotNull(message = "年级不能为空")
    @Pattern(regexp = "^\\d{4}$", message = "年级设置有误")
    private String classGrade;
    @NotNull(message = "必须选择一个专业")
    @PositiveOrZero(message = "选择的专业有误")
    private Long majorId;
}
