package com.taoyyz.framework.web.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/5/4 2:53
 */
@TableName("class")
@Data
@Accessors(chain = true)
public class Clazz {
    @TableId(type = IdType.AUTO)
    private Long classId;
    private String className;
    private String classGrade;
    private Long majorId;
    @TableLogic
    private Integer isDel;
}
