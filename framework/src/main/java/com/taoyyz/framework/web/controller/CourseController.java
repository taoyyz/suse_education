package com.taoyyz.framework.web.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taoyyz.framework.common.Result;
import com.taoyyz.framework.common.utils.LocalDateTimeUtil;
import com.taoyyz.framework.common.utils.RedisUtil;
import com.taoyyz.framework.common.utils.TimeConverter;
import com.taoyyz.framework.common.utils.UserInfoUtil;
import com.taoyyz.framework.web.model.entity.Course;
import com.taoyyz.framework.web.model.entity.CourseSelect;
import com.taoyyz.framework.web.model.entity.User;
import com.taoyyz.framework.web.model.vo.CourseVO;
import com.taoyyz.framework.web.model.vo.ScheduleVO;
import com.taoyyz.framework.web.model.vo.UserVO;
import com.taoyyz.framework.web.service.CourseSelectService;
import com.taoyyz.framework.web.service.CourseService;
import com.taoyyz.framework.web.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/7 14:53
 */
@RestController
@RequestMapping("course")
@Slf4j
@RefreshScope
public class CourseController {
    @Value("${allow.select.course.startTime}")
    private String startTime;
    @Value("${allow.select.course.endTime}")
    private String endTime;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private UserInfoUtil userInfoUtil;
    @Autowired
    private CourseService courseService;
    @Autowired
    private CourseSelectService courseSelectService;
    @Autowired
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 获取选修课程列表
     */
    @GetMapping("listSelectable/{currentPage}/{pageSize}")
    public Result ListElectiveCourse(@PathVariable Integer currentPage, @PathVariable Integer pageSize) throws JsonProcessingException {
        String courseListString = redisUtil.get("course:list");
        if (courseListString == null || courseListString.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        List<CourseVO> courseVOList = objectMapper.readValue(courseListString, new TypeReference<List<CourseVO>>() {
        });
        if (courseVOList.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        //检查当前用户的已选课程id
        Map<Object, Object> selected = redisUtil.hGetAll("course:select:hash");
        Long userId = userInfoUtil.getUserId();
        Set<Long> selectedCourseIds;
        if (!selected.isEmpty()) {
            //获取当前用户已选课程的id
            selectedCourseIds = selected.keySet()
                    .stream()
                    .map(o -> (String) o)
                    .filter(str -> str.endsWith("uid:" + userId))
                    .map(str -> Long.valueOf(str.substring("course:id:".length(), str.lastIndexOf(":uid"))))
                    .collect(Collectors.toSet());
        } else {
            selectedCourseIds = Collections.emptySet();
        }

        //处理已选人数
        List<String> courseIds = courseVOList.stream()
                .map(courseVO -> "course:id:" + courseVO.getCourseId())
                .collect(Collectors.toList());
        List<Integer> selectedCountList = redisUtil.multiGet(courseIds)
                .stream()
                .map(Integer::valueOf)
                .collect(Collectors.toList());
        for (int i = 0; i < courseVOList.size(); i++) {
            //避免redis中数量为负数时造成的已选课人数大于课程容量情况
            CourseVO courseVO = courseVOList.get(i);
            int selectedCount = courseVO.getMaxCount() - selectedCountList.get(i);
            courseVO.setSelectedCount(selectedCount > courseVO.getMaxCount() ? courseVO.getMaxCount() : selectedCount);
            courseVO.setStatus(selectedCourseIds.contains(courseVO.getCourseId()) ? 1 : 0);
        }

        //排序，未满在前
        courseVOList.sort((prev, next) -> {
            int prevIdle = prev.getMaxCount() - prev.getSelectedCount();
            int nextIdle = next.getMaxCount() - next.getSelectedCount();
            int diff = nextIdle - prevIdle;
            //如果diff为0，即要比较的两个课程的剩余课程容量相同，则优先排可退选课程（已选课程）
            if (diff == 0) {
                //如果包含prev，返回负值表示视为prev为小的那个
                if (selectedCourseIds.contains(prev.getCourseId())) {
                    return -1;
                }
            }
            return diff;
        });

        //手动处理分页
        int startIndex = (currentPage - 1) * pageSize;
        courseVOList = courseVOList.stream()
                .skip(startIndex)
                .limit(pageSize)
                .collect(Collectors.toList());
        return Result.success(courseVOList).put("total", courseIds.size());
    }

    /**
     * 选课接口，仅对于选课期间有效
     */
    @PutMapping("{courseId}")
    public Result selectCourse(@PathVariable String courseId) {
        //检查选课时间
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = LocalDateTimeUtil.convertToLocalDateTime(this.startTime);
        LocalDateTime endTime = LocalDateTimeUtil.convertToLocalDateTime(this.endTime);
        if (now.isBefore(startTime) || now.isAfter(endTime)) {
            return Result.error("选课时段已过");
        }

        long userId = userInfoUtil.getUserId();
        //检查选课时段和已有课程时段是否冲突

        //检查当前用户是否已选过指定课程
        String selected = (String) redisUtil.hGet("course:select:hash", "course:id:" + courseId + ":uid:" + userId);
        log.debug("用户 " + userId + " 选了 " + courseId + " -> " + selected);
        if ("true".equals(selected)) {
            return Result.error("您已选过了");
        }
        //课程数量-1
        long count = redisUtil.incrBy("course:id:" + courseId, -1);
        if (count < 0) {
            redisUtil.incrBy("course:id" + courseId, 1);
            return Result.success("课程：" + courseId + "完了");
        }
        try {
            redisUtil.hPut("course:select:hash", "course:id:" + courseId + ":uid:" + userId, "true");
            log.debug("选课成功，courseId：" + courseId + "-> uid：" + userId);
        } catch (Exception e) {
            redisUtil.incrBy("course:id", 1);
            return Result.error("选课失败请重试");
        }
        return Result.success("选课成功");
    }

    /**
     * 取消选课接口，仅对于选课期间有效。
     */
    @PutMapping("reBack/{courseId}")
    public Result reBack(@PathVariable String courseId) throws InterruptedException {
        //检查选课时间
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = LocalDateTimeUtil.convertToLocalDateTime(this.startTime);
        LocalDateTime endTime = LocalDateTimeUtil.convertToLocalDateTime(this.endTime);
        if (now.isBefore(startTime) || now.isAfter(endTime)) {
            return Result.error("选课和退选时段已过");
        }

        long userId = userInfoUtil.getUserId();
        RLock lock = redissonClient.getFairLock("course:id:" + courseId + ":uid:" + userId);
        try {
            boolean allow = lock.tryLock(5, TimeUnit.SECONDS);
            if (allow) {
                //判断是否已选
                String selected = (String) redisUtil.hGet("course:select:hash", "course:id:" + courseId + ":uid:" + userId);
                if (!"true".equals(selected)) {
                    return Result.error("您没有选择此课程");
                }

                //退选课程
                redisUtil.hDelete("course:select:hash", "course:id:" + courseId + ":uid:" + userId);
                //重新添加课程剩余数量
                long count = redisUtil.incrBy("course:id:" + courseId, 1);
                log.debug("用户：" + userId + " -> 退选课程：" + courseId + " 成功，当前余量 = " + count);
                return Result.success("退选成功");
            } else {
                return Result.error("等待超时，请重试");
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取当前学生的课程表
     */
    @GetMapping("schedule")
    public Result getSchedule() {
        long userId = userInfoUtil.getUserId();
        //获取mysql中的所有选课记录
        Set<Long> courseIdsInMySQL = courseSelectService.list(Wrappers.lambdaQuery(CourseSelect.class)
                        .eq(CourseSelect::getUserId, userId))
                .stream()
                .map(CourseSelect::getCourseId)
                .collect(Collectors.toSet());
        //获取redis中所有选课记录
        Set<Object> keys = redisUtil.hKeys("course:select:hash");
        Set<Long> courseIdsInRedis = keys.stream()
                .map(o -> (String) o)
                .filter(s -> s.endsWith("uid:" + userId))
                .map(s -> s.substring("course:id:".length(), s.lastIndexOf(":uid")))
                .map(Long::valueOf)
                .collect(Collectors.toSet());
        if (courseIdsInMySQL.isEmpty() && courseIdsInRedis.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        Set<Long> courseIds = new HashSet<>();
        courseIds.addAll(courseIdsInMySQL);
        courseIds.addAll(courseIdsInRedis);
        //查询这些课程id对应的课程信息
        List<Course> courseList = courseService.listByIds(courseIds);
        //包装为课程表VO对象
        List<ScheduleVO> scheduleVOList = new ArrayList<>(courseList.size());
        //获取所需老师名称列表
        Set<Long> teacherIds = courseList.stream()
                .map(Course::getCreatorId)
                .collect(Collectors.toSet());
        Map<Long, String> userIdToUsername = userService.listByIds(teacherIds)
                .stream()
                .collect(Collectors.toMap(User::getUserId, User::getUsername));
        courseList.forEach(course -> {
            ScheduleVO scheduleVO = new ScheduleVO();
            scheduleVO.setCourseName(course.getCourseName());
            scheduleVO.setTeacherName(userIdToUsername.get(course.getCreatorId()));
            scheduleVO.setLocation(course.getCourseLocation());
            scheduleVO.setTime(course.getCourseTime());
            scheduleVOList.add(scheduleVO);
        });
        return Result.success(scheduleVOList);
    }

    /**
     * 分页获取当前用户的课程列表
     */
    @GetMapping("list/{currentPage}/{pageSize}")
    public Result listCourse(@PathVariable Integer currentPage, @PathVariable Integer pageSize) {
        UserVO userVO = userInfoUtil.getUserVO();
        //获取用户选课详情
        Set<Object> keys = redisUtil.hKeys("course:select:hash");
        List<Long> selectedCourseIds = keys.stream()
                .map(o -> (String) o)
                .filter(str -> userVO.getUserId().toString().equals(str.substring(str.lastIndexOf("uid:") + "uid:".length())))
                .map(str -> Long.valueOf(str.substring("course:id:".length(), str.lastIndexOf(":uid"))))
                .collect(Collectors.toList());
        log.debug("用户id:" + userVO.getUserId() + " -> " + userVO.getUsername() + " 选择了：" + selectedCourseIds);

        List<Course> courseList;
        int needCount = currentPage * pageSize;
        int startIndex = (currentPage - 1) * pageSize;
        List<Long> courseIds;
        long count = courseSelectService.list(Wrappers.lambdaQuery(CourseSelect.class)
                        .eq(CourseSelect::getUserId, userVO.getUserId())
                        .notIn(!selectedCourseIds.isEmpty(), CourseSelect::getCourseId, selectedCourseIds))
                .size() + selectedCourseIds.size();
        //全部是redis中的情况
        if (selectedCourseIds.size() >= needCount) {
            courseIds = selectedCourseIds.stream()
                    .skip(startIndex)
                    .limit(pageSize)
                    .collect(Collectors.toList());
        } else {
            //否则redis中数量可能不足的情况
            //如果包含了一部分redis中的
            if (selectedCourseIds.size() > startIndex) {
                //先从redis读取前inRedis个数据，skip(startIndex).limit(inRedis)
                int inRedis = selectedCourseIds.size() % pageSize;
                List<Long> courseIdsInRedis = selectedCourseIds.stream()
                        .skip(startIndex)
                        .limit(inRedis)
                        .collect(Collectors.toList());
                //再从数据库读取不足的数据，同时跳过已被读取过的,limit 0,inMySQL
                int inMySQL = needCount - selectedCourseIds.size();
                List<Long> courseIdsInMySQL = courseSelectService.list(Wrappers.lambdaQuery(CourseSelect.class)
                                .in(CourseSelect::getUserId, userVO.getUserId())
                                .notIn(!selectedCourseIds.isEmpty(), CourseSelect::getCourseId, selectedCourseIds)
                                .last("limit 0," + inMySQL))
                        .stream()
                        .map(CourseSelect::getCourseId)
                        .collect(Collectors.toList());
                courseIds = new ArrayList<>(courseIdsInRedis.size() + courseIdsInMySQL.size());
                courseIds.addAll(courseIdsInRedis);
                courseIds.addAll(courseIdsInMySQL);
            } else {
                //全部从数据库读取,limit start,pageSize
                int start = needCount - selectedCourseIds.size() - pageSize;
                courseIds = courseSelectService.list(Wrappers.lambdaQuery(CourseSelect.class)
                                .in(CourseSelect::getUserId, userVO.getUserId())
                                .notIn(!selectedCourseIds.isEmpty(), CourseSelect::getCourseId, selectedCourseIds)
                                .last("limit " + start + "," + pageSize))
                        .stream()
                        .map(CourseSelect::getCourseId)
                        .collect(Collectors.toList());
            }
        }
        List<CourseVO> courseVOList;
        if (courseIds.isEmpty()) {
            courseVOList = Collections.emptyList();
        } else {
            courseList = courseService.listByIds(courseIds);
            //填充属性
            courseVOList = populateField(userVO.getUserId(), keys, courseList);
        }
        return Result.success(courseVOList).put("total", count);
        //TODO 优先显示可退订的选课详情，也就是选课还没结束的，SQL查询时LIMIT start,size + 可退订数量，保持可退订课程在已选课列表最前面
    }

    @PutMapping("{courseId}/{num}")
    public Result setCourse(@PathVariable String courseId, @PathVariable String num) {
        //设置课程数量到redis
        boolean set = redisUtil.setIfAbsent("course:id:" + courseId, num);
        return set ? Result.success("设置课程：" + courseId + " -> " + num + "成功") : Result.error("已设置过该课程数量");
        //TODO 需要设置定时任务检查选课时间结束后从redis删除课程以结束选课流程
    }

    /**
     * 分页获取当前用户的课程分数列表
     */
    @GetMapping("score/{currentPage}/{pageSize}")
    public Result listScore(@PathVariable Integer currentPage, @PathVariable Integer pageSize) {
        UserVO userVO = userInfoUtil.getUserVO();
        //获取用户选课详情
        Set<Object> keys = redisUtil.hKeys("course:select:hash");
        List<Long> selectedCourseIds = keys.stream()
                .map(o -> (String) o)
                .filter(str -> userVO.getUserId().toString().equals(str.substring(str.lastIndexOf("uid:") + "uid:".length())))
                .map(str -> Long.valueOf(str.substring("course:id:".length(), str.lastIndexOf(":uid"))))
                .collect(Collectors.toList());

        List<Course> courseList;
        int needCount = currentPage * pageSize;
        int startIndex = (currentPage - 1) * pageSize;
        List<Long> courseIds;
        long count = courseSelectService.list(Wrappers.lambdaQuery(CourseSelect.class)
                        .eq(CourseSelect::getUserId, userVO.getUserId())
                        .notIn(!selectedCourseIds.isEmpty(), CourseSelect::getCourseId, selectedCourseIds))
                .size() + selectedCourseIds.size();
        //全部是redis中的情况
        if (selectedCourseIds.size() >= needCount) {
            courseIds = selectedCourseIds.stream()
                    .skip(startIndex)
                    .limit(pageSize)
                    .collect(Collectors.toList());
        } else {
            //否则redis中数量可能不足的情况
            //如果包含了一部分redis中的
            if (selectedCourseIds.size() > startIndex) {
                //先从redis读取前inRedis个数据，skip(startIndex).limit(inRedis)
                int inRedis = selectedCourseIds.size() % pageSize;
                List<Long> courseIdsInRedis = selectedCourseIds.stream()
                        .skip(startIndex)
                        .limit(inRedis)
                        .collect(Collectors.toList());
                //再从数据库读取不足的数据，同时跳过已被读取过的,limit 0,inMySQL
                int inMySQL = needCount - selectedCourseIds.size();
                List<Long> courseIdsInMySQL = courseSelectService.list(Wrappers.lambdaQuery(CourseSelect.class)
                                .in(CourseSelect::getUserId, userVO.getUserId())
                                .notIn(!selectedCourseIds.isEmpty(), CourseSelect::getCourseId, selectedCourseIds)
                                .last("limit 0," + inMySQL))
                        .stream()
                        .map(CourseSelect::getCourseId)
                        .collect(Collectors.toList());
                courseIds = new ArrayList<>(courseIdsInRedis.size() + courseIdsInMySQL.size());
                courseIds.addAll(courseIdsInRedis);
                courseIds.addAll(courseIdsInMySQL);
            } else {
                //全部从数据库读取,limit start,pageSize
                int start = needCount - selectedCourseIds.size() - pageSize;
                courseIds = courseSelectService.list(Wrappers.lambdaQuery(CourseSelect.class)
                                .in(CourseSelect::getUserId, userVO.getUserId())
                                .notIn(!selectedCourseIds.isEmpty(), CourseSelect::getCourseId, selectedCourseIds)
                                .last("limit " + start + "," + pageSize))
                        .stream()
                        .map(CourseSelect::getCourseId)
                        .collect(Collectors.toList());
            }
        }
        List<CourseVO> courseVOList;
        if (courseIds.isEmpty()) {
            courseVOList = Collections.emptyList();
        } else {
            courseList = courseService.listByIds(courseIds);
            //填充属性
            courseVOList = populateField(userVO.getUserId(), keys, courseList);
        }
        return Result.success(courseVOList).put("total", count);
    }

    /**
     * 设置课程的已选人数
     *
     * @param userId
     * @param keys
     * @param courseList
     */
    private List<CourseVO> populateField(Long userId, Set<Object> keys, List<Course> courseList) {
        //检查是否已过选课时间
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = LocalDateTimeUtil.convertToLocalDateTime(this.startTime);
        LocalDateTime endTime = LocalDateTimeUtil.convertToLocalDateTime(this.endTime);
        //没过选课时间，从redis读取选课数量
//        if (now.isAfter(startTime) && now.isBefore(endTime)) {
        ArrayList<CourseVO> courseVOList = new ArrayList<>();
        Set<Long> teacherIds = courseList.stream()
                .map(Course::getCreatorId)
                .collect(Collectors.toSet());
        Map<Long, String> userIdToName = userService.listByIds(teacherIds)
                .stream()
                .collect(Collectors.toMap(User::getUserId, User::getUsername));
        Set<Long> courseIds = courseList.stream()
                .map(Course::getCourseId)
                .collect(Collectors.toSet());
        List<CourseSelect> courseSelectList = courseSelectService.list(Wrappers.lambdaQuery(CourseSelect.class)
                .eq(CourseSelect::getUserId, userId)
                .in(CourseSelect::getCourseId, courseIds));
        Map<Long, CourseSelect> courseIdToCourseSelect = courseSelectList.stream()
                .collect(Collectors.toMap(CourseSelect::getCourseId, courseSelect -> courseSelect));

        courseList.forEach(course -> {
            CourseVO courseVO = new CourseVO();
            BeanUtils.copyProperties(course, courseVO);
            long courseId = course.getCourseId();
            long count = keys.stream()
                    .map(o -> (String) o)
                    .filter(s -> s.substring("course:id:".length(), s.lastIndexOf(":uid")).equals(String.valueOf(courseId)))
                    .count();
            courseVO.setSelectedCount(count == 0 ? course.getSelectedCount() : (int) count);
            courseVO.setCourseTime(TimeConverter.covertTimePart(course.getCourseTime()));
            courseVO.setCreatorName(userIdToName.get(course.getCreatorId()));
            CourseSelect courseSelect = courseIdToCourseSelect.get(course.getCourseId());
            courseVO.setDailyScore(courseSelect == null ? "-" : courseSelect.getDailyScore());
            courseVO.setExamScore(courseSelect == null ? "-" : courseSelect.getExamScore());
            courseVO.setTotalScore(courseSelect == null ? "-" : courseSelect.getTotalScore());
            courseVO.setType(courseSelect == null ? "选修" : courseSelect.getType());
            courseVOList.add(courseVO);
        });
//        }
        return courseVOList;
    }

    /**
     * 每隔10分钟检查一次选课结束时间，如果redis存在选课缓存则同步到mysql，然后删除redis缓存的选课情况
     */
//    @Scheduled(fixedDelay = 600_000L)
    @GetMapping("sync")
    public void syncRedisToMysql() {
        //如果到达选课开始时间
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime beginTime = LocalDateTimeUtil.convertToLocalDateTime(this.startTime);
        LocalDateTime endTime = LocalDateTimeUtil.convertToLocalDateTime(this.endTime);
        //如果超过截止时间，且在下一年的开始时间之前：选课结束
        if (now.isAfter(endTime) && now.isBefore(beginTime.plusYears(1L))) {
            //读取redis所有的选课记录
            Map<Object, Object> selectedDetailMap = redisUtil.hGetAll("course:select:hash");
            Set<Object> selectedCourseSet = selectedDetailMap.keySet();
            //得到选课的课程id和选课学生id的映射
            Map<String, Set<String>> courseIdToUserIds = selectedCourseSet.stream()
                    .map(o -> (String) o)
                    .collect(Collectors.groupingBy(str -> str.substring("course:id:".length(), str.lastIndexOf(":uid")), Collectors.toSet()));
            System.out.println("courseIdToUserIds = " + courseIdToUserIds);
            //同步到Mysql
            log.info("同步选课信息从redis到mysql完成，课程数量：" + courseIdToUserIds.size());
        }
    }

    /**
     * 每隔5分钟从数据库读取即将开选的课程，缓存到redis
     */
//    @Scheduled(fixedDelay = 300_000L)
    @PutMapping("refresh")
    public void autoCacheCourse() throws JsonProcessingException {
        //判断选课开始时间是否接近5分钟
        LocalDateTime startTime = LocalDateTimeUtil.convertToLocalDateTime(this.startTime);
        LocalDateTime now = LocalDateTime.now();
        //开始选课的提前1分钟之外仍然可以更新redis缓存，而1分钟内不再更新选修课的redis缓存
//        if (now.isBefore(startTime.minusMinutes(1))) {
        //读取选修课程列表缓存到redis
        List<Course> electivesList = courseService.list(Wrappers.lambdaQuery(Course.class)
                .eq(Course::getType, 0));
        if (electivesList.isEmpty()) {
            return;
        }
        Map<String, String> courseIdToCount = new HashMap<>();
        electivesList.forEach(course -> {
            courseIdToCount.put("course:id:" + course.getCourseId(), String.valueOf(course.getMaxCount()));
        });
        List<CourseVO> courseVOList = new ArrayList<>(electivesList.size());
        convertToVO(courseVOList, electivesList);
        String courseVOString = objectMapper.writeValueAsString(courseVOList);
        redisUtil.multiSet(courseIdToCount);
        redisUtil.set("course:list", courseVOString);
        log.info("缓存选修课程：" + electivesList.size() + "条");
        /*} else {
            log.info("已在选课时段内");
        }*/
    }

    private void convertToVO(List<CourseVO> courseVOList, List<Course> electivesList) {
        Set<Long> creatorIds = electivesList.stream()
                .map(Course::getCreatorId)
                .collect(Collectors.toSet());
        Map<Long, Long> courseIdToCreatorId = electivesList.stream()
                .collect(Collectors.toMap(Course::getCourseId, Course::getCreatorId));
        Map<Long, String> userIdToName = userService.listByIds(creatorIds).stream()
                .collect(Collectors.toMap(User::getUserId, User::getUsername));
        electivesList.forEach(course -> {
            CourseVO courseVO = new CourseVO();
            BeanUtils.copyProperties(course, courseVO);
            courseVO.setCreatorName(userIdToName.get(courseIdToCreatorId.get(course.getCourseId())));
            courseVO.setCourseTime(TimeConverter.covertTimePart(course.getCourseTime()));
            courseVOList.add(courseVO);
        });
    }

    /**
     * 刷新选课时间
     */
    @PutMapping("refreshSelectTime")
    public Result refreshSelectTime() {
        LocalDateTime selectTime = LocalDateTimeUtil.convertToLocalDateTime(this.startTime);
        return null;
    }
}
