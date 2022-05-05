package com.taoyyz.framework.web.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.Size;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/5/4 17:56
 */
@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DepartmentRequest {
    private Long departmentId;

    @Size(min = 2, max = 30, message = "学院/系部名称必须为2到30个字符")
    private String departmentName;
}
