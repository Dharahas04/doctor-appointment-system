package com.app.doctorappointment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DoctorAppointmentApplication {
	public static void main(String[] args) {
		SpringApplication.run(DoctorAppointmentApplication.class, args);
	}
}
