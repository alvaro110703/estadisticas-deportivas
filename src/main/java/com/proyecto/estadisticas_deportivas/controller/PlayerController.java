package com.proyecto.estadisticas_deportivas.controller;

import com.proyecto.estadisticas_deportivas.model.Player;
import com.proyecto.estadisticas_deportivas.repository.PlayerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/players")
@CrossOrigin(origins = "http://localhost:5173")
public class PlayerController {

    @Autowired
    private PlayerRepo playerRepo;

    // 1. Obtener todos
    @GetMapping
    public List<Player> getAllPlayers() {
        return playerRepo.findAll();
    }

    // 2. Crear un nuevo jugador (POST)
    @PostMapping
    public Player createPlayer(@RequestBody Player player) {
        return playerRepo.save(player);
    }

    // 3. Buscar por ID
    @GetMapping("/{id}")
    public Player getPlayerById(@PathVariable Long id) {
        return playerRepo.findById(id).orElse(null);
    }

    // 4. Borrar un jugador
    @DeleteMapping("/{id}")
    public void deletePlayer(@PathVariable Long id) {
        playerRepo.deleteById(id);
    }

    @GetMapping("/team/{teamName}")
    public List<Player> getPlayersByTeam(@PathVariable String teamName) {
        return playerRepo.findByTeam(teamName);
    }

    // Buscador por nombre: /api/players/search?name=vini
    @GetMapping("/search")
    public List<Player> searchPlayers(@RequestParam String name) {
        return playerRepo.findByNameContainingIgnoreCase(name);
    }
}