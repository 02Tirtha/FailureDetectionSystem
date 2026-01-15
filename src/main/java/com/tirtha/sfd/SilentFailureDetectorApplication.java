package com.tirtha.sfd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@SpringBootApplication
@EnableScheduling
public class SilentFailureDetectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(SilentFailureDetectorApplication.class, args);
	}

}
