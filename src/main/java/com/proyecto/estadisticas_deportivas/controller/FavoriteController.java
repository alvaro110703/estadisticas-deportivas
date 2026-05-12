package com.proyecto.estadisticas_deportivas.controller;

import com.proyecto.estadisticas_deportivas.model.Player;
import com.proyecto.estadisticas_deportivas.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/favorites")
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

    @GetMapping("/{userId}")
    public ResponseEntity<Set<Player>> getFavorites(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getFavorites(userId));
    }
}