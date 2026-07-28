package com.alibaba.ticketsystem;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.nio.file.Paths;

public class CodeGenerator {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/ticket_system?useSSL=false&characterEncoding=utf8&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "root";

        FastAutoGenerator.create(url, username, password)
                .globalConfig(builder -> builder
                        .author("YanTao")
                        .outputDir(Paths.get(System.getProperty("user.dir")) + "/src/main/java")
                        .commentDate("yyyy-MM-dd")
                )
                .packageConfig(builder -> builder
                        .parent("com.alibaba.ticketsystem")  //包路径
                        .entity("entity")
                        .mapper("mapper")
                        .service("service")
                        .serviceImpl("service.impl")
                        .xml("mapper.xml")
                )
                .strategyConfig(builder -> builder
                        .entityBuilder()
                        .enableLombok()
                )
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();

    }
}
