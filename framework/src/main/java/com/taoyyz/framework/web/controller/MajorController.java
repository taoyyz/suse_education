package com.taoyyz.framework.web.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import com.taoyyz.framework.common.Result;
import com.taoyyz.framework.common.constant.CommonRedisKey;
import com.taoyyz.framework.common.utils.RedisUtil;
import com.taoyyz.framework.web.model.VO.MajorVO;
import com.taoyyz.framework.web.model.entity.Major;
import com.taoyyz.framework.web.model.request.MajorRequest;
import com.taoyyz.framework.web.service.MajorService;
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
 * @since 2022/4/5 16:06
 */
@RestController
@RequestMapping("major")
@Slf4j
public class MajorController {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private MajorService majorService;
    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("admin/list/{currentPage}/{pageSize}")
    public Result listAll(@PathVariable Integer currentPage,
                          @PathVariable Integer pageSize,
                          @RequestParam(required = false) Long departmentId,
                          @RequestParam(required = false) String majorName) {
        PageInfo pageInfo = majorService.listByConditions(currentPage, pageSize, departmentId, majorName);
        return Result.success(pageInfo);
    }

    @GetMapping("admin/{id}")
    public Result get(@PathVariable Long id) {
        MajorVO majorVO = majorService.getMajorVO(id);
        return majorVO == null ? Result.error("专业不存在") : Result.success(majorVO);
    }

    @PostMapping("admin/add")
    public Result add(@RequestBody @Validated MajorRequest majorRequest) {
        Major major = new Major()
                .setMajorName(majorRequest.getMajorName())
                .setDepartmentId(majorRequest.getDepartmentId());
        boolean saved = majorService.save(major);
        return saved ? Result.success("创建成功") : Result.error("创建失败");
    }

    @PutMapping("admin/edit")
    public Result edit(@RequestBody @Validated MajorRequest majorRequest) {
        boolean updated = majorService.update(Wrappers.lambdaUpdate(Major.class)
                .set(Major::getMajorName, majorRequest.getMajorName())
                .set(Major::getDepartmentId, majorRequest.getDepartmentId())
                .eq(Major::getMajorId, majorRequest.getMajorId()));
        return updated ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("admin/del/{id}")
    public Result del(@PathVariable Long id) {
        boolean deleted = majorService.removeById(id);
        return deleted ? Result.success("删除成功") : Result.error("删除失败");
    }

    @GetMapping("admin/listAll")
    public Result listAll() {
        List<Major> majorList = majorService.list();
        return Result.success(majorList);
    }

    @GetMapping("admin/listWithId")
    public Result listWithDepartmentId(@RequestParam(required = false) Long departmentId) {
        List<Major> majorList = majorService.list(Wrappers.lambdaQuery(Major.class)
                .eq(departmentId != null, Major::getDepartmentId, departmentId));
        return Result.success(majorList);
    }

    @PostConstruct
    void init() throws JsonProcessingException {
        List<Major> majorList = majorService.listAll();
        Map<Long, Major> map = majorList.stream()
                .collect(Collectors.toMap(Major::getMajorId, major -> major));
        String mapString = objectMapper.writeValueAsString(map);
        redisUtil.set(CommonRedisKey.MAJOR_MAP_KEY, mapString);
        log.info("初始化专业映射完成：" + (map.isEmpty() ? "暂无专业" : ("共" + map.size() + "个专业")));
    }

    /**
     * @return 专业id到专业对象的映射
     */
    public Map<Long, Major> getIdToMajor() throws JsonProcessingException {
        String mapString = redisUtil.get(CommonRedisKey.MAJOR_MAP_KEY);
        //获得redis中专业id到专业对象的映射
        return objectMapper
                .readValue(mapString, new TypeReference<Map<Long, Major>>() {
                });
    }
}
