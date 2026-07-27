package com.alibaba.ticketsystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.alibaba.ticketsystem.mapper") //配置映射器扫描器
public class TicketsystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketsystemApplication.class, args);
    }

}
