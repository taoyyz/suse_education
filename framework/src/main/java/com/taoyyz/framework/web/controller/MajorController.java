package com.taoyyz.framework.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taoyyz.framework.common.constant.CommonRedisKey;
import com.taoyyz.framework.common.utils.RedisUtil;
import com.taoyyz.framework.web.model.entity.Major;
import com.taoyyz.framework.web.service.MajorService;
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

    @PostConstruct
    private void init() throws JsonProcessingException {
        String existedString = redisUtil.get(CommonRedisKey.MAJOR_MAP_KEY);
        if (existedString == null) {
            List<Major> majorList = majorService.list();
            Map<Long, Major> map = majorList.stream()
                    .collect(Collectors.toMap(Major::getMajorId, major -> major));
            String mapString = objectMapper.writeValueAsString(map);
            redisUtil.set(CommonRedisKey.MAJOR_MAP_KEY, mapString);
            log.info("初始化专业映射完成：" + (map.isEmpty() ? "暂无专业" : ("共" + map.size() + "个专业")));
        }
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
