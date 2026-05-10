package com.proyecto.estadisticas_deportivas.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity // Esto le dice a Spring: "Crea una tabla con esta clase"
@Data   // Esto genera automáticamente Getters y Setters (gracias a Lombok)
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String team;
    private String position;

    // Estadísticas para el radar
    private int pace;
    private int shooting;
    private int passing;
    private int dribbling;
    private int defense;
    private int physical;
}