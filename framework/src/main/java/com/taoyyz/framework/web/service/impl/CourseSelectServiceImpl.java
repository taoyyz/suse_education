package com.taoyyz.framework.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taoyyz.framework.web.mapper.CourseSelectMapper;
import com.taoyyz.framework.web.model.entity.CourseSelect;
import com.taoyyz.framework.web.service.CourseSelectService;
import org.springframework.stereotype.Service;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/16 22:51
 */
@Service
public class CourseSelectServiceImpl extends ServiceImpl<CourseSelectMapper, CourseSelect> implements CourseSelectService {
}
