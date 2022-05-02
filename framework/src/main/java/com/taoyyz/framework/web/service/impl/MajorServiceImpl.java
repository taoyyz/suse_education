package com.taoyyz.framework.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taoyyz.framework.web.mapper.MajorMapper;
import com.taoyyz.framework.web.model.entity.Major;
import com.taoyyz.framework.web.service.MajorService;
import org.springframework.stereotype.Service;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/5 16:06
 */
@Service
public class MajorServiceImpl extends ServiceImpl<MajorMapper, Major> implements MajorService {
}
