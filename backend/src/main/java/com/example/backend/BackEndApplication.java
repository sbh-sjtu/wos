package com.example.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(exclude = JpaRepositoriesAutoConfiguration.class)
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