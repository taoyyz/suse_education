package com.taoyyz.framework.web.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/5 15:58
 */
@Data
@Accessors(chain = true)
public class Department {
    @TableId
    private Long departmentId;
    private String departmentName;
    private Integer isDel;
}
