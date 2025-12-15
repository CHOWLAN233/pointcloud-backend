package com.pointcloud.backend;

import org.mybatis.spring.annotation.MapperScan;//Import package
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.pointcloud.backend.mapper")//Point to the mapper package
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
