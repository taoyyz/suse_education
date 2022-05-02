package com.taoyyz.framework.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taoyyz.framework.web.mapper.CourseMapper;
import com.taoyyz.framework.web.model.entity.Course;
import com.taoyyz.framework.web.service.CourseService;
import org.springframework.stereotype.Service;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/16 21:26
 */
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {
}
