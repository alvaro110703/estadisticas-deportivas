package com.proyecto.estadisticas_deportivas.config;

import com.proyecto.estadisticas_deportivas.model.Player;
import com.proyecto.estadisticas_deportivas.repository.PlayerRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(PlayerRepo playerRepo) {
        return args -> {
            if (playerRepo.count() == 0) { // Solo mete datos si la tabla está vacía
                Player p1 = new Player();
                p1.setName("Lionel Messi");
                p1.setTeam("Inter Miami");
                p1.setPosition("FW");
                p1.setPace(80);
                p1.setShooting(90);
                playerRepo.save(p1);

                Player p2 = new Player();
                p2.setName("Cristiano Ronaldo");
                p2.setTeam("Al-Nassr");
                p2.setPosition("ST");
                p2.setPace(85);
                p2.setShooting(92);
                playerRepo.save(p2);

                System.out.println("Base de datos inicializada con jugadores de prueba.");
            }
        };
    }
}