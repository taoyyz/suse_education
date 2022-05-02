package com.taoyyz.framework.web.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/4 0:01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class User {
    /**
     * 用户唯一主键id
     */
    @TableId
    private Long userId;
    /**
     * 学号/职工号
     */
    private String number;
    /**
     * 姓名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 性别
     */
    private String gender;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 生日
     */
    private LocalDate birthDay;
    /**
     * 用户类型：1学生，2老师，3管理员
     */
    private Integer userType;
    /**
     * 所属学院/系部id
     */
    private Long departmentId;
    /**
     * 所属专业id
     */
    private Long majorId;
    /**
     * 所属班级id
     */
    private Long clazzId;
    /**
     * 创建时间
     */
    private LocalDateTime creatTime;
    /**
     * 上次登录时间
     */
    private LocalDateTime lastLoginTime;
    /**
     * 登录计数
     */
    private Integer loginCount;
    /**
     * 逻辑删除，0未删除，1已删除
     */
    private Integer isDel;

}
