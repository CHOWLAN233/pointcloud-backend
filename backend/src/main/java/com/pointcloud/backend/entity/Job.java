package com.pointcloud.backend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("jobs")//Tell MP the jobs table in the corresponding database
public class Job {
    @TableId(type = IdType.ASSIGN_ID)//MP's built-in snowflake algorithm generates IDs
    private String jobId;
    
    private String status;
    private Integer progress;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
