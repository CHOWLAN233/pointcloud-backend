package com.pointcloud.backend.controller;

import com.pointcloud.backend.entity.Job;
import com.pointcloud.backend.mapper.JobMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController//Tells Spring that this is a controller, and that all method return values ​​will be automatically converted to JSON format.
@RequestMapping("/pointcloud/jobs")//Defines the base URL path that this controller handles.
public class JobController {

    @Autowired
    private JobMapper jobMapper;//Injecting Mapper

    //Create task
    @PostMapping
    public Map<String, Object> createJob(@RequestBody Map<String, Object> request) {
        //1. Create a new Job entity object
        Job job = new Job();

        //2. Set the initial state
        job.setStatus("PENDING");//Initial state is set to waiting
        job.setProgress(0);//Set the initial progress to 0
        job.setCreateTime(LocalDateTime.now());//Set the creation time to the current time
        job.setUpdateTime(LocalDateTime.now());//Set the update time to the current time
        
        //3. Save to database
        jobMapper.insert(job); //JPA is for save, MP is for insert

        //4. Construct the JSON data to be returned to the front end
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("data", Map.of("jobId", job.getJobId(), "status", "PENDING"));
        return response;
    }

    //Query status
    @GetMapping("/{jobId}")
    public Map<String, Object> getJobStatus(@PathVariable String jobId) {
        Job job = jobMapper.selectById(jobId);//JPA uses findById,MP uses selectById

        Map<String, Object> response = new HashMap<>();
        if (job != null) 
        {
            response.put("code", 0);
            response.put("data", job);
        } 
        else 
        {
            response.put("code", 4041);
        }
        return response;
    }
}
