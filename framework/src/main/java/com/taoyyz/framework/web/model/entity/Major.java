package com.taoyyz.framework.web.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/5 15:59
 */
@Data
@Accessors(chain = true)
public class Major {
    @TableId
    private Long majorId;
    private String majorName;
    private Long departmentId;
    private Integer isDel;
}
