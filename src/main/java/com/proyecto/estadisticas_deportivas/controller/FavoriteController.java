package com.proyecto.estadisticas_deportivas.controller;

import com.proyecto.estadisticas_deportivas.model.Player;
import com.proyecto.estadisticas_deportivas.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.Set;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = "http://localhost:5173")
public class FavoriteController {

    @Autowired
    private UserService userService;

    @PostMapping("/add/{userId}")
    public ResponseEntity<?> addFavorite(@PathVariable Long userId, @RequestBody Player player) {
        userService.addFavorite(userId, player);
        return ResponseEntity.ok("Jugador añadido a favoritos");
    }

    @DeleteMapping("/remove/{userId}/{playerId}")
    public ResponseEntity<?> removeFavorite(@PathVariable Long userId, @PathVariable String playerId) {
        userService.removeFavorite(userId, playerId);
        return ResponseEntity.ok("Jugador eliminado de favoritos");
    }

    @GetMapping
    public ResponseEntity<Set<Player>> getFavorites(Authentication authentication) {
        // authentication.getName() nos da el CORREO (o username) con el que se logueó
        // en Postman
        String email = authentication.getName();
        return ResponseEntity.ok(userService.getFavoritesByEmail(email));
    }
}