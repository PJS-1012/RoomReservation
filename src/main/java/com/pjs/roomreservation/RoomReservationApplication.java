package com.pjs.roomreservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class RoomReservationApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoomReservationApplication.class, args);
	}

}
