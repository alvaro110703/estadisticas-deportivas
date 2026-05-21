package com.proyecto.estadisticas_deportivas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {
    private String nombre;
    private String correo;
    private String contrasena;
}