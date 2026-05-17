package com.proyecto.estadisticas_deportivas.config;

import com.proyecto.estadisticas_deportivas.model.MatchPlayerStat;
import com.proyecto.estadisticas_deportivas.model.Player;
import com.proyecto.estadisticas_deportivas.repository.MatchPlayerStatRepo;
import com.proyecto.estadisticas_deportivas.repository.PlayerRepo; // Asegúrate de usar el nombre exacto de tu repo
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    // @Bean
    // CommandLineRunner initDatabase(PlayerRepo playerRepo, MatchPlayerStatRepo statRepo) {
    //     return args -> {
    //         // Solo insertamos si la tabla de jugadores está completamente vacía
    //         if (playerRepo.count() == 0) { 
                
    //             // 1. Crear e insertar a Kylian Mbappé
    //             Player mbappe = new Player();
    //             mbappe.setName("Kylian Mbappe");
    //             mbappe.setTeam("Real Madrid");
    //             mbappe.setPosition("FW");
    //             // Guardamos el jugador para que la DB le asigne su ID (será el 1)
    //             playerRepo.save(mbappe); 

    //             // 2. Crear partido de LOCAL (2 goles, 1 asistencia, titular)
    //             MatchPlayerStat statHome = new MatchPlayerStat();
    //             statHome.setPlayer(mbappe); // Lo vinculamos a Mbappé
    //             statHome.setMatchId("MATCH-001");
    //             statHome.setHome(true);
    //             statHome.setStarter(true);
    //             statHome.setGoals(2);
    //             statHome.setAssists(1);
    //             statRepo.save(statHome);

    //             // 3. Crear partido de VISITANTE (1 gol, 0 asistencias, titular)
    //             MatchPlayerStat statAway = new MatchPlayerStat();
    //             statAway.setPlayer(mbappe); // Lo vinculamos a Mbappé
    //             statAway.setMatchId("MATCH-002");
    //             statAway.setHome(false);
    //             statAway.setStarter(true);
    //             statAway.setGoals(1);
    //             statAway.setAssists(0);
    //             statRepo.save(statAway);

    //             System.out.println(">> Base de datos inicializada AUTOMÁTICAMENTE con Mbappé y sus estadísticas.");
    //         }
    //     };
    // }
}