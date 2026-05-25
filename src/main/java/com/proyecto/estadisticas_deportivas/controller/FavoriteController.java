package com.proyecto.estadisticas_deportivas.controller;

import com.proyecto.estadisticas_deportivas.model.Player;
import com.proyecto.estadisticas_deportivas.model.User;
import com.proyecto.estadisticas_deportivas.repository.UserRepo;
import com.proyecto.estadisticas_deportivas.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.Set;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class FavoriteController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepo userRepo;

    @PostMapping("/add-by-email/{email}")
    public ResponseEntity<?> addFavoriteByEmail(@PathVariable String email, @RequestBody Player player) {
        User user = userRepo.findByCorreo(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 🌟 Imprimimos el valor del objeto user asignado
        System.out.println("=== USUARIO ASIGNADO ===");
        System.out.println("ID: " + user.getId());
        System.out.println("Nombre: " + user.getNombre());
        System.out.println("Correo: " + user.getCorreo());
        System.out.println("========================");

        userService.addFavorite(user.getId(), player);
        return ResponseEntity.ok("Jugador añadido a favoritos");
    }

    @DeleteMapping("/remove-by-email/{email}/{playerId}")
    public ResponseEntity<?> removeFavoriteByEmail(@PathVariable String email, @PathVariable String playerId) {
        // Buscamos al usuario por correo para obtener su ID real de la base de datos
        User user = userRepo.findByCorreo(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Reutilizamos tu método de servicio existente
        userService.removeFavorite(user.getId(), playerId);

        return ResponseEntity.ok("Jugador eliminado de favoritos");
    }

    @GetMapping
    public ResponseEntity<Set<Player>> getFavorites(Authentication authentication) {
        // authentication.getName() nos da el CORREO (o username) con el que se logueó
        // en Postman
        String email = authentication.getName();
        return ResponseEntity.ok(userService.getFavoritesByEmail(email));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Set<Player>> getFavoritesByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getFavoritesByUserId(userId));
    }

    // Cambiamos el GetMapping para que reciba el correo directamente en la ruta
    @GetMapping("/user-email/{email}")
    public ResponseEntity<Set<Player>> getFavoritesByEmailRoute(@PathVariable String email) {
        return ResponseEntity.ok(userService.getFavoritesByEmail(email));
    }
}