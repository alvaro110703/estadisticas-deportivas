package com.proyecto.estadisticas_deportivas.controller;

import com.proyecto.estadisticas_deportivas.dto.PlayerStatsDTO;
import com.proyecto.estadisticas_deportivas.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/players")
public class PlayerStatsController {

    @Autowired
    private PlayerService playerService; // Asegúrate de que apunte a PlayerService ahora

    @GetMapping("/{playerId}/stats")
    public ResponseEntity<PlayerStatsDTO> getPlayerStats(
            @PathVariable Long playerId,
            @RequestParam(defaultValue = "true") boolean isHome) { 
        
        return ResponseEntity.ok(playerService.getPlayerStatsFiltered(playerId, isHome));
    }
}