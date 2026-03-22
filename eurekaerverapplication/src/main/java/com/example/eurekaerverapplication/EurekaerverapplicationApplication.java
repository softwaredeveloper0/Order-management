package com.example.eurekaerverapplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer

public class EurekaerverapplicationApplication {

	public static void main(String[] args) {
		SpringApplication.run(EurekaerverapplicationApplication.class, args);
	}

}
