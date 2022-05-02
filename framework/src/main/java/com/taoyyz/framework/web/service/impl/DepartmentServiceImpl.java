package com.taoyyz.framework.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taoyyz.framework.web.mapper.DepartmentMapper;
import com.taoyyz.framework.web.model.entity.Department;
import com.taoyyz.framework.web.service.DepartmentService;
import org.springframework.stereotype.Service;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/5 16:01
 */
@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {
}
