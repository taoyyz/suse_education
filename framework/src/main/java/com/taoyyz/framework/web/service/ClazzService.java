package com.taoyyz.framework.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.taoyyz.framework.web.model.DTO.ClazzDTO;
import com.taoyyz.framework.web.model.entity.Clazz;

import java.util.List;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/5/4 2:54
 */
public interface ClazzService extends IService<Clazz> {
    List<ClazzDTO> listAll();

    PageInfo listByConditions(Integer currentPage, Integer pageSize, Long departmentId, Long majorId, String className);
}
