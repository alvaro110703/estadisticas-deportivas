package com.proyecto.estadisticas_deportivas.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "match_player_stats")
@Data
public class MatchPlayerStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con el jugador: Muchos registros de estadísticas pertenecen a un jugador
    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    private String matchId;     // ID del partido (de la API)
    private boolean isHome;     // true = Local, false = Visitante
    private boolean isStarter;  // true = Titular, false = Suplente
    private int goals;
    private int assists;
}