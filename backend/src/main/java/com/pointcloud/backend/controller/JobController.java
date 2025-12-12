package com.pointcloud.backend.controller;

import com.pointcloud.backend.entity.Job;
import com.pointcloud.backend.mapper.JobMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pointcloud/jobs")
public class JobController {

    @Autowired
    private JobMapper jobMapper; // 注入 Mapper

    // 创建任务
    @PostMapping
    public Map<String, Object> createJob(@RequestBody Map<String, Object> request) {
        Job job = new Job();
        // ID 由 MP 自动生成，这里不需要手动 setJobId
        job.setStatus("PENDING");
        job.setProgress(0);
        job.setCreateTime(LocalDateTime.now());
        job.setUpdateTime(LocalDateTime.now());

        jobMapper.insert(job); // JPA是save, MP是insert

        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("data", Map.of("jobId", job.getJobId(), "status", "PENDING"));
        return response;
    }

    // 查询状态
    @GetMapping("/{jobId}")
    public Map<String, Object> getJobStatus(@PathVariable String jobId) {
        Job job = jobMapper.selectById(jobId); // JPA是findById, MP是selectById

        Map<String, Object> response = new HashMap<>();
        if (job != null) {
            response.put("code", 0);
            response.put("data", job);
        } else {
            response.put("code", 4041);
        }
        return response;
    }
}
