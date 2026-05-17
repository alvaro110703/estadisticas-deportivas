package com.proyecto.estadisticas_deportivas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerStatsDTO {
    private String playerName;
    private int totalGoals;
    private int totalAssists;
    private long matchesPlayed;
    private long matchesAsStarter;
}
