package com.pointcloud.backend.repository;

import com.pointcloud.backend.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<Job, String> {
    // 这里空着就行，Spring 会自动实现基本的增删改查
}
