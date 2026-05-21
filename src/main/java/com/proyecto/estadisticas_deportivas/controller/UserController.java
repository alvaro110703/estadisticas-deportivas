package com.proyecto.estadisticas_deportivas.controller;

import com.proyecto.estadisticas_deportivas.dto.LoginRequestDTO;
import com.proyecto.estadisticas_deportivas.model.User;
import com.proyecto.estadisticas_deportivas.repository.UserRepo;
import com.proyecto.estadisticas_deportivas.service.UserService;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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
            // 1. Buscamos al usuario por correo usando el repositorio (o un método del
            // service)
            User usuario = userRepo.findByCorreo(loginRequest.getCorreo())
                    .orElseThrow(() -> new RuntimeException("Usuario o contraseña incorrectos."));

            // 2. Comparamos la contraseña en texto plano del front con la encriptada de la
            // DB
            if (!passwordEncoder.matches(loginRequest.getContrasena(), usuario.getContrasena())) {
                throw new RuntimeException("Usuario o contraseña incorrectos.");
            }

            // 3. Si todo está bien, le devolvemos al frontend los datos básicos que
            // necesita pintar
            // Devolvemos un mapa o un objeto limpio (¡NUNCA devuelvas la contraseña al
            // front!)
            Map<String, String> response = new HashMap<>();
            response.put("nombre", usuario.getNombre());
            response.put("correo", usuario.getCorreo());
            response.put("rol", usuario.getRol());
            response.put("id", String.valueOf(usuario.getId()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Si falla, mandamos el mensaje de error que React pintará en la caja roja
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}