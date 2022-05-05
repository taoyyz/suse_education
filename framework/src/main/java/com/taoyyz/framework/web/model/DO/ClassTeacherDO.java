package com.taoyyz.framework.web.model.DO;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/5/4 3:01
 */
@Data
@Accessors(chain = true)
@TableName("class_teacher")
public class ClassTeacherDO {
    private Long tbId;
    private Long classId;
    private Long teacherId;
}
