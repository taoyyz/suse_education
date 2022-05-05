package com.taoyyz.framework.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.taoyyz.framework.web.model.DTO.CourseScoreDTO;
import com.taoyyz.framework.web.model.entity.Course;
import com.taoyyz.framework.web.model.entity.CourseSelect;
import com.taoyyz.framework.web.model.request.ScoreRequest;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/16 21:26
 */
public interface CourseService extends IService<Course> {
    PageInfo<CourseScoreDTO> listScoreList(Integer currentPage, Integer pageSize, String keyword, Long courseId);

    CourseSelect getScoreById(Long tbId);

    boolean updateScore(ScoreRequest scoreRequest);
}
