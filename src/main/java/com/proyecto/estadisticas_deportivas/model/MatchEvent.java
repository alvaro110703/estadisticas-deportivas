package com.proyecto.estadisticas_deportivas.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "match_events")
@Data // Genera los getters, setters y constructores gracias a Lombok
public class MatchEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String matchId;     // El ID del partido para saber a cuál pertenece este evento
    private String eventType;   // Qué pasó: "Goal" (Gol) o "Assist" (Asistencia)
    private String playerName;  // Nombre del jugador que lo hizo ("Kylian Mbappe")
    private String apiPlayerId; // El ID que le da la API al jugador (por si lo necesitamos)
    private int minute;         // Minuto en el que pasó (minuto 23, minuto 80...)
    private boolean isHomeEvent;// true si lo hizo el equipo local, false si el visitante
}