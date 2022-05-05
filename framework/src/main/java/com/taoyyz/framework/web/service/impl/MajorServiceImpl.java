package com.taoyyz.framework.web.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.taoyyz.framework.web.controller.DepartmentController;
import com.taoyyz.framework.web.mapper.ClazzMapper;
import com.taoyyz.framework.web.mapper.MajorMapper;
import com.taoyyz.framework.web.mapper.UserMapper;
import com.taoyyz.framework.web.model.VO.MajorVO;
import com.taoyyz.framework.web.model.entity.Clazz;
import com.taoyyz.framework.web.model.entity.Department;
import com.taoyyz.framework.web.model.entity.Major;
import com.taoyyz.framework.web.model.entity.User;
import com.taoyyz.framework.web.service.MajorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/5 16:06
 */
@Service
public class MajorServiceImpl extends ServiceImpl<MajorMapper, Major> implements MajorService {
    @Autowired
    private DepartmentController departmentController;
    @Autowired
    private ClazzMapper clazzMapper;
    @Autowired
    private MajorMapper majorMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public PageInfo listByConditions(Integer currentPage, Integer pageSize, Long departmentId, String majorName) {
        Page<Object> page = PageHelper.startPage(currentPage, pageSize);
        List<Major> majorList = this.list(Wrappers.lambdaQuery(Major.class)
                .eq(departmentId != null && departmentId > 0, Major::getDepartmentId, departmentId)
                .like(StringUtils.hasText(majorName), Major::getMajorName, majorName)
                .orderByAsc(Major::getDepartmentId));
        PageInfo<Major> majorPageInfo = PageInfo.of(majorList);
        page.close();

        if (majorList.isEmpty()) {
            return majorPageInfo;
        }

        List<MajorVO> majorVOList = new ArrayList<>(majorList.size());
        Set<Long> majorIds = majorList.stream().map(Major::getMajorId).collect(Collectors.toSet());
        Map<Long, Long> majorIdToClazzCount = clazzMapper.selectList(Wrappers.lambdaQuery(Clazz.class)
                        .in(Clazz::getMajorId, majorIds))
                .stream()
                .collect(Collectors.groupingBy(Clazz::getMajorId, Collectors.counting()));
        Map<Long, Long> majorIdToStudentCount = userMapper.selectList(Wrappers.lambdaQuery(User.class)
                        .eq(User::getUserType, 1)
                        .in(User::getMajorId, majorIds))
                .stream()
                .collect(Collectors.groupingBy(User::getMajorId, Collectors.counting()));
        majorList.forEach(major -> {
            String departmentName;
            try {
                Optional<Department> department = Optional.ofNullable(departmentController.getIdToDepartment().get(major.getDepartmentId()));
                departmentName = department.map(Department::getDepartmentName).orElse("");
            } catch (JsonProcessingException e) {
                departmentName = "";
            }
            Long clazzCount = majorIdToClazzCount.get(major.getMajorId());
            Long studentCount = majorIdToStudentCount.get(major.getMajorId());
            MajorVO majorVO = new MajorVO()
                    .setMajorId(major.getMajorId())
                    .setMajorName(major.getMajorName())
                    .setDepartmentName(departmentName)
                    .setClassCount(clazzCount == null ? 0 : clazzCount)
                    .setStudentCount(studentCount == null ? 0 : studentCount);

            majorVOList.add(majorVO);
        });
        PageInfo<MajorVO> majorVOPageInfo = PageInfo.of(majorVOList);
        majorVOPageInfo.setTotal(majorPageInfo.getTotal());
        return majorVOPageInfo;
    }

    @Override
    public MajorVO getMajorVO(Long id) {
        Major major = this.getById(id);
        if (major == null) {
            return null;
        }

        String departmentName;
        try {
            Optional<Department> department = Optional.ofNullable(departmentController.getIdToDepartment().get(major.getDepartmentId()));
            departmentName = department.map(Department::getDepartmentName).orElse("");
        } catch (JsonProcessingException e) {
            departmentName = "";
        }
        return new MajorVO()
                .setMajorId(major.getMajorId())
                .setMajorName(major.getMajorName())
                .setDepartmentName(departmentName);
    }

    @Override
    public List<Major> listAll() {
        return majorMapper.listAll();
    }
}
