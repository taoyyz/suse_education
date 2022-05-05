package com.taoyyz.framework.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import com.taoyyz.framework.common.Result;
import com.taoyyz.framework.common.constant.CommonRedisKey;
import com.taoyyz.framework.common.utils.RedisUtil;
import com.taoyyz.framework.web.model.entity.Department;
import com.taoyyz.framework.web.model.request.DepartmentRequest;
import com.taoyyz.framework.web.service.DepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/5 16:02
 */
@Slf4j
@RestController
@RequestMapping("department")
public class DepartmentController {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("list/{currentPage}/{pageSize}")
    public Result list(@PathVariable Integer currentPage, @PathVariable Integer pageSize, @RequestParam(required = false) String departmentName) {
        PageInfo pageInfo = departmentService.listByConditions(currentPage, pageSize, departmentName);
        return Result.success(pageInfo);
    }

    @GetMapping("admin/{id}")
    public Result get(@PathVariable Long id) {
        Department department = departmentService.getById(id);
        return Result.success(department);
    }

    @PostMapping("admin/add")
    public Result add(@RequestBody @Validated DepartmentRequest departmentRequest) {
        Department department = new Department().setDepartmentName(departmentRequest.getDepartmentName());
        boolean success = departmentService.save(department);
        return success ? Result.success("创建成功") : Result.error("创建失败");
    }

    @PutMapping("admin/edit")
    public Result update(@RequestBody @Validated DepartmentRequest departmentRequest) {
        boolean success = departmentService.updateName(departmentRequest);
        return success ? Result.success("修改成功") : Result.error("修改学院/系部名称失败");
    }

    @DeleteMapping("admin/del/{id}")
    public Result update(@PathVariable Long id) {
        boolean removed = departmentService.removeById(id);
        return removed ? Result.success("删除成功") : Result.error("删除失败");
    }

    @GetMapping("admin/listAll")
    public Result listAll() {
        List<Department> departmentList = departmentService.list();
        return Result.success(departmentList);
    }


    @PostConstruct
    void init() throws JsonProcessingException {
        List<Department> departmentList = departmentService.listAll();
        Map<Long, Department> map = departmentList.stream()
                .collect(Collectors.toMap(Department::getDepartmentId, department -> department));
        String mapString = objectMapper.writeValueAsString(map);
        redisUtil.set(CommonRedisKey.DEPARTMENT_MAP_KEY, mapString);
        log.info("初始化学院/系部映射完成：" + (map.isEmpty() ? "暂无学院/系部" : ("共" + map.size() + "个学院/系部")));
    }

    /**
     * @return 学院/系部id到学院/系部对象的映射
     */
    public Map<Long, Department> getIdToDepartment() throws JsonProcessingException {
        String mapString = redisUtil.get(CommonRedisKey.DEPARTMENT_MAP_KEY);
        //获得redis中学院/系部id到学院/系部对象的映射
        return objectMapper
                .readValue(mapString, new TypeReference<Map<Long, Department>>() {
                });
    }
}
