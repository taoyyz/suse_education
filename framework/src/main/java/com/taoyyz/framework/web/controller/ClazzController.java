package com.taoyyz.framework.web.controller;

import com.taoyyz.framework.common.Result;
import com.taoyyz.framework.common.annotation.Auth;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/4 01:34
 */
@RestController
@RequestMapping("class")
public class ClazzController {

    @GetMapping("{id}")
    @Auth(minAuthRequire = 1)
    public Result getById(@PathVariable Long id) {
        return Result.error("");
    }
}
