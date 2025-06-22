package com.leostormer.strife;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@EnableMongoAuditing
public class StrifeApplication {

	public static void main(String[] args) {
		SpringApplication.run(StrifeApplication.class, args);
	}

	@GetMapping("/")
	public String apiRoot() {
		return "Welcome to Strife!";
	}
}
