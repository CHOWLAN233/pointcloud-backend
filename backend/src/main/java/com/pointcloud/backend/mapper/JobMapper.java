package com.pointcloud.backend.mapper;

import com.pointcloud.backend.entity.Job;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper // 关键注解
public interface JobMapper extends BaseMapper<Job> {
    // 这一行代码写完，你已经拥有了 insert, delete, update, selectById 等几十个方法！
    // 这就是 MyBatis-Plus 的威力。
}