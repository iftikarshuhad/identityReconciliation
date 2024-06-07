package com.idendity.recon;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReconApplication {
	private static final Logger logger = LoggerFactory.getLogger(ReconApplication.class);


	public static void main(String[] args) {
		SpringApplication.run(ReconApplication.class, args);
		logger.info("Application started successfully");
	}

}
