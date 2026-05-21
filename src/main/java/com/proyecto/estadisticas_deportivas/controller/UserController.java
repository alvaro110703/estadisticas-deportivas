package com.proyecto.estadisticas_deportivas.controller;

import com.proyecto.estadisticas_deportivas.dto.LoginRequestDTO;
import com.proyecto.estadisticas_deportivas.model.User;
import com.proyecto.estadisticas_deportivas.repository.UserRepo;
import com.proyecto.estadisticas_deportivas.service.UserService;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepo userRepo;

    @PostMapping("/register")
    public ResponseEntity<?> registrar(@Valid @RequestBody User user) {
        try {
            User nuevoUsuario = userService.registrarUsuario(user);
            return ResponseEntity.ok("Usuario " + nuevoUsuario.getNombre() + " registrado con éxito.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Autowired
    private PasswordEncoder passwordEncoder; // Para comparar las contraseñas

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {
        try {
            // Buscamos al usuario tanto por el nombre como por el correo usando el mismo
            // valor
            User usuario = userRepo
                    .findByNombreOrCorreo(loginRequest.getIdentificador(), loginRequest.getIdentificador())
                    .orElseThrow(() -> new RuntimeException("Usuario o contraseña incorrectos."));

            // Comparamos la contraseña encriptada
            if (!passwordEncoder.matches(loginRequest.getContrasena(), usuario.getContrasena())) {
                throw new RuntimeException("Usuario o contraseña incorrectos.");
            }

            // Estructuramos la respuesta para React
            Map<String, String> response = new HashMap<>();
            response.put("id", String.valueOf(usuario.getId()));
            response.put("nombre", usuario.getNombre());
            response.put("correo", usuario.getCorreo());
            response.put("rol", usuario.getRol());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}