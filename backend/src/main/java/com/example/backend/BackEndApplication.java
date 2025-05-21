package com.example.backend;

import org.mybatis.spring.annotation.MapperScan;   //MyBatis 的注解，用于扫描 Mapper 接口
import org.springframework.boot.SpringApplication;    //Spring Boot 的核心类，用于启动应用程序。
import org.springframework.boot.autoconfigure.SpringBootApplication;    //Spring Boot 的核心注解，标记这是主启动类。
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;    //Spring Data JPA 的自动配置类（这里被排除了，因为项目用的是 MyBatis，不是 JPA）。
import org.springframework.boot.builder.SpringApplicationBuilder;    //用于构建 Spring Boot 应用（支持 WAR 包部署）。
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;    //支持将 Spring Boot 应用部署到外部 Servlet 容器（如 Tomcat）。




@SpringBootApplication(exclude = JpaRepositoriesAutoConfiguration.class)    //标记这是 Spring Boot 的主启动类，并配置自动加载行为。
@MapperScan("com.example.backend.mapper")
public class BackEndApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(BackEndApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(BackEndApplication.class);
	}

}
