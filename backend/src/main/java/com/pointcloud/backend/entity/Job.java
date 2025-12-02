package com.pointcloud.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data // 自动生成 Getter, Setter, ToString
@Entity // 告诉 Spring 这是一个数据库表
@Table(name = "jobs")
public class Job {
    @Id
    private String jobId;       // 任务ID (主键)
    
    private String status;      // PENDING, RUNNING, SUCCEEDED
    private Integer progress;   // 0 - 100
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 为了简化，这里暂时省略复杂的 stages JSON 字段，先跑通核心流程
}
