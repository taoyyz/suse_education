package com.taoyyz.framework.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taoyyz.framework.web.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/5 1:12
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
