package ru.Darvin.ApplicationRegistrationService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = "ru.Darvin")
@EntityScan(basePackages = "ru.Darvin.Entity")
@EnableJpaRepositories(basePackages = "ru.Darvin.Repository")
public class ApplicationRegistrationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApplicationRegistrationServiceApplication.class, args);
	}

}
