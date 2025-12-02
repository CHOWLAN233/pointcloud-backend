package com.pointcloud.backend.controller;

import com.pointcloud.backend.entity.Job;
import com.pointcloud.backend.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/pointcloud/jobs")
@EnableAsync // 开启异步处理能力
public class JobController {

    @Autowired
    private JobRepository jobRepository;

    // 1. 创建任务接口 [POST /pointcloud/jobs]
    @PostMapping
    public Map<String, Object> createJob(@RequestBody Map<String, Object> request) {
        // 生成 ID
        String jobId = "pc-job-" + UUID.randomUUID().toString().substring(0, 8);

        // 初始化任务对象
        Job job = new Job();
        job.setJobId(jobId);
        job.setStatus("PENDING");
        job.setProgress(0);
        job.setCreateTime(LocalDateTime.now());
        job.setUpdateTime(LocalDateTime.now());

        // 存入数据库
        jobRepository.save(job);

        // 关键：触发异步处理（假装在跑算法）
        simulateAlgorithm(jobId);

        // 立即返回给前端，不让用户等
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("message", "ok");
        response.put("data", Map.of("jobId", jobId, "status", "PENDING"));
        return response;
    }

    // 2. 查询状态接口 [GET /pointcloud/jobs/{jobId}]
    @GetMapping("/{jobId}")
    public Map<String, Object> getJobStatus(@PathVariable String jobId) {
        Optional<Job> job = jobRepository.findById(jobId);

        Map<String, Object> response = new HashMap<>();
        if (job.isPresent()) {
            response.put("code", 0);
            response.put("message", "ok");
            response.put("data", job.get());
        } else {
            response.put("code", 4041); // 文档规定的 Job不存在
            response.put("message", "Job not found");
        }
        return response;
    }

    // --- 模拟算法处理的“黑盒”方法 (异步执行) ---
    @Async
    public void simulateAlgorithm(String jobId) {
        try {
            System.out.println("开始处理任务: " + jobId);
            
            // 模拟阶段 1: 正在处理中
            Thread.sleep(2000); // 假装跑了2秒
            updateJobProgress(jobId, "RUNNING", 30);
            
            // 模拟阶段 2: 处理更多
            Thread.sleep(3000); // 又跑了3秒
            updateJobProgress(jobId, "RUNNING", 80);
            
            // 模拟阶段 3: 完成
            Thread.sleep(1000);
            updateJobProgress(jobId, "SUCCEEDED", 100);
            
            System.out.println("任务完成: " + jobId);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 辅助方法：更新数据库
    private void updateJobProgress(String jobId, String status, int progress) {
        Optional<Job> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isPresent()) {
            Job job = jobOpt.get();
            job.setStatus(status);
            job.setProgress(progress);
            job.setUpdateTime(LocalDateTime.now());
            jobRepository.save(job);
        }
    }
}
