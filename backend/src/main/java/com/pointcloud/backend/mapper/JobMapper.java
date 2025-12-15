package com.pointcloud.backend.mapper;

import com.pointcloud.backend.entity.Job;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JobMapper extends BaseMapper<Job> {
    //No need to write sth
}
