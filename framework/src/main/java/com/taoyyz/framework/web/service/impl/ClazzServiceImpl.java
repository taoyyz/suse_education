package com.taoyyz.framework.web.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.taoyyz.framework.common.utils.UserInfoUtil;
import com.taoyyz.framework.web.mapper.*;
import com.taoyyz.framework.web.model.DO.ClassTeacherDO;
import com.taoyyz.framework.web.model.DTO.ClazzDTO;
import com.taoyyz.framework.web.model.VO.ClazzVO;
import com.taoyyz.framework.web.model.entity.Clazz;
import com.taoyyz.framework.web.model.entity.Department;
import com.taoyyz.framework.web.model.entity.Major;
import com.taoyyz.framework.web.model.entity.User;
import com.taoyyz.framework.web.service.ClazzService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/5/4 2:55
 */
@Service
public class ClazzServiceImpl extends ServiceImpl<ClazzMapper, Clazz> implements ClazzService {
    @Autowired
    private UserInfoUtil userInfoUtil;
    @Autowired
    private ClassTeacherMapper classTeacherMapper;
    @Autowired
    private MajorMapper majorMapper;
    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public List<ClazzDTO> listAll() {
        Long userId = userInfoUtil.getUserId();
        List<Long> classIds = classTeacherMapper.selectList(Wrappers.lambdaQuery(ClassTeacherDO.class)
                        .eq(ClassTeacherDO::getTeacherId, userId))
                .stream()
                .map(ClassTeacherDO::getClassId)
                .collect(Collectors.toList());
        if (classIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Clazz> clazzList = this.listByIds(classIds);
        return clazzList.stream()
                .map(clazz -> new ClazzDTO().setClassId(clazz.getClassId()).setClassName(clazz.getClassName()))
                .collect(Collectors.toList());
    }

    @Override
    public PageInfo listByConditions(Integer currentPage, Integer pageSize, Long departmentId, Long majorId, String className) {
        //如果只传递了系，则查询指定系下的所有专业
        Set<Long> majorIds = null;
        if (departmentId != null && majorId == null) {
            majorIds = majorMapper.selectList(Wrappers.lambdaQuery(Major.class)
                            .eq(Major::getDepartmentId, departmentId))
                    .stream()
                    .map(Major::getMajorId)
                    .collect(Collectors.toSet());
        }

        Page<Object> page = PageHelper.startPage(currentPage, pageSize);
        List<Clazz> clazzList = this.list(Wrappers.lambdaQuery(Clazz.class)
                .eq(majorId != null, Clazz::getMajorId, majorId)
                .in(majorIds != null, Clazz::getMajorId, majorIds)
                .like(StringUtils.hasText(className), Clazz::getClassName, className)
                .orderByAsc(Clazz::getClassGrade, Clazz::getClassName));
        page.close();
        PageInfo<Clazz> clazzPageInfo = PageInfo.of(clazzList);
        if (clazzList.isEmpty()) {
            return clazzPageInfo;
        }

        Set<Long> majorIdsSet = clazzList.stream()
                .map(Clazz::getMajorId)
                .collect(Collectors.toSet());
        List<Major> majorList = majorMapper.selectBatchIds(majorIdsSet);
        Set<Long> departmentIdsSet = majorList.stream()
                .map(Major::getDepartmentId)
                .collect(Collectors.toSet());
        Map<Long, Major> majorIdToMajor = majorList
                .stream()
                .collect(Collectors.toMap(Major::getMajorId, major -> major));
        Map<Long, String> departmentIdToName = departmentMapper.selectList(Wrappers.lambdaQuery(Department.class)
                        .in(Department::getDepartmentId, departmentIdsSet))
                .stream()
                .collect(Collectors.toMap(Department::getDepartmentId, Department::getDepartmentName));
        Map<Long, Long> clazzIdToStudentCount = userMapper.selectList(Wrappers.lambdaQuery(User.class)
                        .in(User::getClazzId, clazzList.stream().map(Clazz::getClassId).collect(Collectors.toSet())))
                .stream()
                .collect(Collectors.groupingBy(User::getClazzId, Collectors.counting()));

        //包装为VO对象
        List<ClazzVO> clazzVOList = new ArrayList<>(clazzList.size());
        clazzList.forEach(clazz -> {
            Optional<Major> majorOptional = Optional.ofNullable(majorIdToMajor.get(clazz.getMajorId()));
            String departmentName = departmentIdToName.get(majorOptional.map(Major::getDepartmentId).orElse(null));
            Long studentCount = clazzIdToStudentCount.get(clazz.getClassId());
            ClazzVO clazzVO = new ClazzVO()
                    .setClassId(clazz.getClassId())
                    .setClassName(clazz.getClassName())
                    .setClassGrade(clazz.getClassGrade())
                    .setMajorName(majorOptional.map(Major::getMajorName).orElse(""))
                    .setDepartmentName(Optional.ofNullable(departmentName).orElse(""))
                    .setStudentCount(studentCount == null ? 0 : studentCount);
            clazzVOList.add(clazzVO);
        });
        PageInfo<ClazzVO> clazzVOPageInfo = PageInfo.of(clazzVOList);
        clazzVOPageInfo.setTotal(clazzPageInfo.getTotal());
        return clazzVOPageInfo;
    }
}
