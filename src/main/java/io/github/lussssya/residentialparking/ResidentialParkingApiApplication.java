package io.github.lussssya.residentialparking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ResidentialParkingApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResidentialParkingApiApplication.class, args);
	}

}
