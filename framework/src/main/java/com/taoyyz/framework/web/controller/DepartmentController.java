package com.taoyyz.framework.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taoyyz.framework.common.constant.CommonRedisKey;
import com.taoyyz.framework.common.utils.RedisUtil;
import com.taoyyz.framework.web.model.entity.Department;
import com.taoyyz.framework.web.service.DepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostConstruct
    private void init() throws JsonProcessingException {
        String existedString = redisUtil.get(CommonRedisKey.DEPARTMENT_MAP_KEY);
        if (existedString == null) {
            List<Department> departmentList = departmentService.list();
            Map<Long, Department> map = departmentList.stream()
                    .collect(Collectors.toMap(Department::getDepartmentId, department -> department));
            String mapString = objectMapper.writeValueAsString(map);
            redisUtil.set(CommonRedisKey.DEPARTMENT_MAP_KEY, mapString);
            log.info("初始化学院/系部映射完成：" + (map.isEmpty() ? "暂无学院/系部" : ("共" + map.size() + "个学院/系部")));
        }
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
