package com.proyecto.estadisticas_deportivas.service;

import com.proyecto.estadisticas_deportivas.dto.PlayerStatsDTO;
import com.proyecto.estadisticas_deportivas.model.MatchPlayerStat;
import com.proyecto.estadisticas_deportivas.model.Player;
import com.proyecto.estadisticas_deportivas.repository.MatchPlayerStatRepo;
import com.proyecto.estadisticas_deportivas.repository.PlayerRepo; // Asumo que se llama así tu repo de Player
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service // Le dice a Spring que esta clase es un Servicio gerenciado por él
public class PlayerService {

    @Autowired
    private PlayerRepo playerRepo;

    @Autowired
    private MatchPlayerStatRepo statRepo;

    /**
     * Obtiene las estadísticas acumuladas de un jugador filtrando si es Local o Visitante.
     * * @param playerId   ID del jugador en nuestra base de datos.
     * @param filterHome true para filtrar estadísticas de LOCAL, false para VISITANTE.
     * @return Un DTO con el resumen de goles, asistencias y titularidades.
     */
    public PlayerStatsDTO getPlayerStatsFiltered(Long playerId, boolean filterHome) {
        // 1. Buscamos si el jugador existe en la base de datos
        Player player = playerRepo.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado con ID: " + playerId));

        // 2. Traemos de la DB solo los partidos que cumplan el filtro (Home o Away)
        List<MatchPlayerStat> statsList = statRepo.findByPlayerIdAndIsHome(playerId, filterHome);

        // 3. Inicializamos los contadores para acumular la información
        int totalGoals = 0;
        int totalAssists = 0;
        long matchesAsStarter = 0;

        // 4. Recorremos la lista sumando los datos de cada partido
        for (MatchPlayerStat stat : statsList) {
            totalGoals += stat.getGoals();
            totalAssists += stat.getAssists();
            
            if (stat.isStarter()) {
                matchesAsStarter++;
            }
        }

        // 5. Construimos y devolvemos el DTO con los datos masticados
        return new PlayerStatsDTO(
                player.getName(),
                totalGoals,
                totalAssists,
                statsList.size(), // El tamaño de la lista es el total de partidos jugados bajo ese filtro
                matchesAsStarter
        );
    }
}