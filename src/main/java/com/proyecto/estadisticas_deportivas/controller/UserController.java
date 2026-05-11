package com.proyecto.estadisticas_deportivas.controller;

import com.proyecto.estadisticas_deportivas.model.User;
import com.proyecto.estadisticas_deportivas.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registrar(@Valid @RequestBody User user) {
        try {
            User nuevoUsuario = userService.registrarUsuario(user);
            return ResponseEntity.ok("Usuario " + nuevoUsuario.getNombre() + " registrado con éxito.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}