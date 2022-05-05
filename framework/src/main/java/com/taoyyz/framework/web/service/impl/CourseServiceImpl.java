package com.taoyyz.framework.web.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.taoyyz.framework.common.utils.UserInfoUtil;
import com.taoyyz.framework.web.mapper.CourseMapper;
import com.taoyyz.framework.web.mapper.CourseSelectMapper;
import com.taoyyz.framework.web.model.DTO.CourseScoreDTO;
import com.taoyyz.framework.web.model.entity.Course;
import com.taoyyz.framework.web.model.entity.CourseSelect;
import com.taoyyz.framework.web.model.request.ScoreRequest;
import com.taoyyz.framework.web.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/16 21:26
 */
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private CourseSelectMapper courseSelectMapper;
    @Autowired
    private UserInfoUtil userInfoUtil;

    @Override
    public PageInfo<CourseScoreDTO> listScoreList(Integer currentPage, Integer pageSize, String keyword, Long courseId) {
        Page<Object> page = PageHelper.startPage(currentPage, pageSize);
        List<CourseScoreDTO> courseScoreDTOList = courseMapper.selectScoreList(userInfoUtil.getUserId(), keyword, courseId);
        courseScoreDTOList.forEach(courseScoreDTO -> {
            if (courseScoreDTO.getDailyScore() == null) {
                courseScoreDTO.setDailyScore("-");
            }
            if (courseScoreDTO.getExamScore() == null) {
                courseScoreDTO.setExamScore("-");
            }
            if (courseScoreDTO.getTotalScore() == null) {
                courseScoreDTO.setTotalScore("-");
            }
        });
        PageInfo<CourseScoreDTO> pageInfo = PageInfo.of(courseScoreDTOList);
        page.close();
        return pageInfo;
    }

    @Override
    public CourseSelect getScoreById(Long tbId) {
        return courseSelectMapper.selectById(tbId);
    }

    @Override
    public boolean updateScore(ScoreRequest scoreRequest) {
        return courseSelectMapper.update(null, Wrappers.lambdaUpdate(CourseSelect.class)
                .set(CourseSelect::getDailyScore, scoreRequest.getDailyScore())
                .set(CourseSelect::getExamScore, scoreRequest.getExamScore())
                .set(CourseSelect::getTotalScore, scoreRequest.getTotalScore())
                .eq(CourseSelect::getTbId, scoreRequest.getTbId())) == 1;
    }
}
