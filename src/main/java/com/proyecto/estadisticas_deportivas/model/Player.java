package com.proyecto.estadisticas_deportivas.model;

import jakarta.persistence.*;
import lombok.Data; // Si usas Lombok

@Entity
@Table(name = "player")
@Data // Si usas Lombok, los getters/setters se generan solos. Si no, añádelos a mano abajo.
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String position;
    private String team;

    // === NUEVOS CAMPOS ===
    @Column(name = "api_player_id", unique = true)
    private String apiPlayerId; // El ID de la API (ej: "34147178" para Mbappé)

    @Column(name = "total_goals")
    private int totalGoals = 0; // Contador acumulado de goles

    @Column(name = "total_assists")
    private int totalAssists = 0; // Contador acumulado de asistencias

    // Si NO usas Lombok, recuerda generar aquí los Getters y Setters para estos 3 campos.
}