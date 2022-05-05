package com.taoyyz.framework.web.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import com.taoyyz.framework.common.Result;
import com.taoyyz.framework.common.annotation.Auth;
import com.taoyyz.framework.common.constant.CommonRedisKey;
import com.taoyyz.framework.common.enums.ApiCode;
import com.taoyyz.framework.common.utils.RedisUtil;
import com.taoyyz.framework.web.model.DTO.ClazzDTO;
import com.taoyyz.framework.web.model.entity.Clazz;
import com.taoyyz.framework.web.model.request.ClazzRequest;
import com.taoyyz.framework.web.service.ClazzService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/4 01:34
 */
@RestController
@RequestMapping("class")
@Slf4j
public class ClazzController {
    @Autowired
    private ClazzService clazzService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private RedissonClient redissonClient;

    @GetMapping("admin/{id}")
    public Result get(@PathVariable Long id) {
        Clazz clazz = clazzService.getById(id);
        return clazz == null ? Result.error("班级不存在") : Result.success(clazz);
    }

    @PostMapping("admin/add")
    public Result add(@RequestBody @Validated ClazzRequest clazzRequest) throws InterruptedException {
        RLock lock = redissonClient.getLock(CommonRedisKey.LOCK_CLASS_NAME + clazzRequest.getClassName());
        try {
            boolean locked = lock.tryLock(5, TimeUnit.SECONDS);
            if (locked) {
                //在同一年级中班级名称不能相同
                Clazz one = clazzService.getOne(Wrappers.lambdaQuery(Clazz.class)
                        .eq(Clazz::getClassName, clazzRequest.getClassName())
                        .eq(Clazz::getClassGrade, clazzRequest.getClassGrade())
                        .last("limit 1"));
                if (one != null) {
                    return Result.error("班级名称已存在");
                }

                Clazz clazz = new Clazz()
                        .setClassName(clazzRequest.getClassName())
                        .setClassGrade(clazzRequest.getClassGrade())
                        .setMajorId(clazzRequest.getMajorId());
                boolean saved = clazzService.save(clazz);
                return saved ? Result.success("创建班级成功") : Result.error("创建班级失败");
            }
            return Result.error(ApiCode.NO_LOCK);
        } finally {
            lock.unlock();
        }
    }

    @PutMapping("admin/edit")
    public Result edit(@RequestBody @Validated ClazzRequest clazzRequest) throws InterruptedException {
        RLock lock = redissonClient.getLock(CommonRedisKey.LOCK_CLASS_NAME + clazzRequest.getClassName());
        try {
            boolean locked = lock.tryLock(5, TimeUnit.SECONDS);
            if (locked) {
                //除自身以外，同一年级中班级名称不能相同
                Clazz one = clazzService.getOne(Wrappers.lambdaQuery(Clazz.class)
                        .eq(Clazz::getClassName, clazzRequest.getClassName())
                        .eq(Clazz::getClassGrade, clazzRequest.getClassGrade())
                        .ne(Clazz::getClassId, clazzRequest.getClassId())
                        .last("limit 1"));
                if (one != null) {
                    return Result.error("班级名称已存在");
                }

                boolean updated = clazzService.update(Wrappers.lambdaUpdate(Clazz.class)
                        .set(Clazz::getClassName, clazzRequest.getClassName())
                        .set(Clazz::getClassGrade, clazzRequest.getClassGrade())
                        .set(Clazz::getMajorId, clazzRequest.getMajorId())
                        .eq(Clazz::getClassId, clazzRequest.getClassId()));
                return updated ? Result.success("修改成功") : Result.error("修改失败");
            }
            return Result.error(ApiCode.NO_LOCK);
        } finally {
            lock.unlock();
        }
    }

    @DeleteMapping("admin/del/{id}")
    public Result del(@PathVariable Long id) {
        boolean removed = clazzService.removeById(id);
        return removed ? Result.success("删除成功") : Result.error("删除失败");
    }

    @GetMapping("admin/list/{currentPage}/{pageSize}")
    public Result listWithConditions(@PathVariable Integer currentPage,
                                     @PathVariable Integer pageSize,
                                     @RequestParam(required = false) Long departmentId,
                                     @RequestParam(required = false) Long majorId,
                                     @RequestParam(required = false) String className) {
        PageInfo pageInfo = clazzService.listByConditions(currentPage, pageSize, departmentId, majorId, className);
        return Result.success(pageInfo);
    }

    @GetMapping("teacher/listAll")
    @Auth(minAuthRequire = 1)
    public Result listAll() {
        List<ClazzDTO> clazzDTOList = clazzService.listAll();
        return Result.success(clazzDTOList);
    }

    @PostConstruct
    void init() throws JsonProcessingException {
        List<Clazz> clazzList = clazzService.list();
        Map<Long, Clazz> map = clazzList.stream()
                .collect(Collectors.toMap(Clazz::getClassId, clazz -> clazz));
        String mapString = objectMapper.writeValueAsString(map);
        redisUtil.set(CommonRedisKey.CLASS_MAP_KEY, mapString);
        log.info("初始化班级映射完成：" + (map.isEmpty() ? "暂无班级" : ("共" + map.size() + "个班级")));
    }

    /**
     * @return 班级id到班级对象的映射
     */
    public Map<Long, Clazz> getIdToClazz() throws JsonProcessingException {
        String mapString = redisUtil.get(CommonRedisKey.CLASS_MAP_KEY);
        //获得redis中学院/系部id到学院/系部对象的映射
        return objectMapper
                .readValue(mapString, new TypeReference<Map<Long, Clazz>>() {
                });
    }
}
