package com.callbackcats.reboarding;

import employee.EmployeeImporter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
public class ReboardingApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReboardingApplication.class, args);
	}

}
