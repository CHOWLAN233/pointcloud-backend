package com.pointcloud.backend.mapper;

import com.pointcloud.backend.entity.AiMove;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper // 告诉 Spring Boot 这是一个操作数据库的接口
public interface AiMoveMapper extends BaseMapper<AiMove> {
    // 这里依然什么都不用写！
    // 继承了 BaseMapper，你自动就拥有了 insert, update, selectById 等神技。
}
