package com.taoyyz.framework.web.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.taoyyz.framework.common.utils.RedisUtil;
import com.taoyyz.framework.web.mapper.DepartmentMapper;
import com.taoyyz.framework.web.mapper.MajorMapper;
import com.taoyyz.framework.web.mapper.UserMapper;
import com.taoyyz.framework.web.model.VO.DepartmentVO;
import com.taoyyz.framework.web.model.entity.Department;
import com.taoyyz.framework.web.model.entity.Major;
import com.taoyyz.framework.web.model.entity.User;
import com.taoyyz.framework.web.model.request.DepartmentRequest;
import com.taoyyz.framework.web.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/5 16:01
 */
@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private MajorMapper majorMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public PageInfo listByConditions(Integer currentPage, Integer pageSize, String departmentName) {
        Page<Object> page = PageHelper.startPage(currentPage, pageSize);
        List<Department> departmentList = this.list(Wrappers.lambdaQuery(Department.class)
                .like(StringUtils.hasText(departmentName), Department::getDepartmentName, departmentName));
        page.close();
        PageInfo<Department> pageInfo = PageInfo.of(departmentList);
        if (departmentList.isEmpty()) {
            return pageInfo;
        }

        //专业数量
        Set<Long> departmentIds = departmentList.stream()
                .map(Department::getDepartmentId)
                .collect(Collectors.toSet());
        List<Major> majorList = majorMapper.selectList(Wrappers.lambdaQuery(Major.class)
                .in(Major::getDepartmentId, departmentIds));
        Map<Long, Long> departmentIdToMajorCount = majorList.stream()
                .collect(Collectors.groupingBy(Major::getDepartmentId, Collectors.counting()));
        //教师数量
        List<User> userList = userMapper.selectList(Wrappers.lambdaQuery(User.class)
                .in(User::getDepartmentId, departmentIds)
                .eq(User::getUserType, 2));
        Map<Long, Long> departmentIdToTeacherCount = userList.stream()
                .collect(Collectors.groupingBy(User::getDepartmentId, Collectors.counting()));

        List<DepartmentVO> departmentVOList = new ArrayList<>(departmentList.size());
        departmentList.forEach(department -> {
            Long majorCount = departmentIdToMajorCount.get(department.getDepartmentId());
            Long teacherCount = departmentIdToTeacherCount.get(department.getDepartmentId());
            DepartmentVO departmentVO = new DepartmentVO()
                    .setDepartmentId(department.getDepartmentId())
                    .setDepartmentName(department.getDepartmentName())
                    .setMajorCount(majorCount == null ? 0 : majorCount)
                    .setTeacherCount(teacherCount == null ? 0 : teacherCount);
            departmentVOList.add(departmentVO);
        });
        PageInfo<DepartmentVO> departmentVOPageInfo = PageInfo.of(departmentVOList);
        departmentVOPageInfo.setTotal(pageInfo.getTotal());
        departmentVOPageInfo.setPages(pageInfo.getPages());
        return departmentVOPageInfo;
    }

    @Override
    public boolean updateName(DepartmentRequest departmentRequest) {
        return this.update(Wrappers.lambdaUpdate(Department.class)
                .set(Department::getDepartmentName, departmentRequest.getDepartmentName())
                .eq(Department::getDepartmentId, departmentRequest.getDepartmentId()));
    }

    @Override
    public List<Department> listAll() {
        return departmentMapper.listAll();
    }
}
