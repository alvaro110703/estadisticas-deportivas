package com.proyecto.estadisticas_deportivas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EstadisticasDeportivasApplication {

	public static void main(String[] args) {
		SpringApplication.run(EstadisticasDeportivasApplication.class, args);
	}

	@Bean
	public org.springframework.web.client.RestTemplate restTemplate() {
		return new org.springframework.web.client.RestTemplate();
	}

}
