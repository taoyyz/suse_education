package com.taoyyz.framework.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.taoyyz.framework.web.model.VO.MajorVO;
import com.taoyyz.framework.web.model.entity.Major;

import java.util.List;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/5 16:06
 */
public interface MajorService extends IService<Major> {
    PageInfo listByConditions(Integer currentPage, Integer pageSize, Long departmentId, String majorName);

    MajorVO getMajorVO(Long id);

    List<Major> listAll();
}
