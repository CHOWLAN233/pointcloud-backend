package com.pointcloud.backend;

import org.mybatis.spring.annotation.MapperScan; // 导入包
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.pointcloud.backend.mapper") // <--- 加上这一行，指向你的 mapper 包
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}