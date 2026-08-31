package com.mendes.scheduling_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SchedulingPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(SchedulingPlatformApplication.class, args);
	}

}
