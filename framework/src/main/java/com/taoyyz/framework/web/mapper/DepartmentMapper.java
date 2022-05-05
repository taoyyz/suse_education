package com.taoyyz.framework.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taoyyz.framework.web.model.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/5 16:01
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
    @Select("select * from department")
    List<Department> listAll();
}
