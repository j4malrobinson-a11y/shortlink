package com.nageoffer.shortlink.project;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.nageoffer.shortlink.project.dao.mapper")
public class ShortLinkprojectApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShortLinkprojectApplication.class,args);
    }
}
