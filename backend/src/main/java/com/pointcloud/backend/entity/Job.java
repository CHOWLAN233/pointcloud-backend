package com.pointcloud.backend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("jobs") // 告诉 MP 对应数据库的 jobs 表
public class Job {
    @TableId(type = IdType.ASSIGN_ID) // MP 自带的雪花算法生成 ID，不用 UUID 了
    private String jobId;
    
    private String status;
    private Integer progress;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
