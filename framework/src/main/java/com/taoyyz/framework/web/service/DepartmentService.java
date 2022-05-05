package com.taoyyz.framework.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.taoyyz.framework.web.model.entity.Department;
import com.taoyyz.framework.web.model.request.DepartmentRequest;

import java.util.List;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/5 16:00
 */
public interface DepartmentService extends IService<Department> {
    PageInfo listByConditions(Integer currentPage, Integer pageSize, String departmentName);

    boolean updateName(DepartmentRequest departmentRequest);

    List<Department> listAll();
}
