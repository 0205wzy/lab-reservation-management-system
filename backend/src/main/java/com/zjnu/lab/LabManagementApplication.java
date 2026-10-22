package com.zjnu.lab;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan("com.zjnu.lab.mapper")
@SpringBootApplication
@EnableScheduling
public class LabManagementApplication {
    public static void main(String[] args) { SpringApplication.run(LabManagementApplication.class, args); }
}
