package com.proyecto.estadisticas_deportivas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.estadisticas_deportivas.service.PlayerSimulationService;

@RestController
@RequestMapping("/api/test-simulation")
public class TestSimulationController {

    @Autowired
    private PlayerSimulationService playerSimulationService;

    @PostMapping("/rayo")
    public ResponseEntity<String> probarRepartoRayo() {
        try {
            // Forzamos el nombre del equipo para la prueba aislada
            playerSimulationService.repartirYLimpiarSimulaciones("Rayo Vallecano");
            return ResponseEntity.ok("Reparto completado con éxito y filas 'SIM' borradas para el Rayo Vallecano.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error durante la simulación: " + e.getMessage());
        }
    }

    @PostMapping("/laliga")
    public ResponseEntity<String> procesarLigaEspanola() {
        try {
            playerSimulationService.procesarSoloLigaEspanola();
            return ResponseEntity
                    .ok("Éxito: Se han limpiado las simulaciones exclusivamente para los equipos de la Liga Española.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error en el procesado: " + e.getMessage());
        }
    }

    @PostMapping("/premier")
    public ResponseEntity<String> procesarPremierLeague() {
        try {
            playerSimulationService.procesarSoloPremierLeague();
            return ResponseEntity.ok(
                    "Éxito: Se han limpiado las simulaciones exclusivamente para los equipos de la Premier League.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error en el procesado de la Premier: " + e.getMessage());
        }
    }

    @PostMapping("/seriea")
    public ResponseEntity<String> procesarSerieA() {
        try {
            playerSimulationService.procesarSoloSerieA();
            return ResponseEntity.ok("Éxito: Se han limpiado las simulaciones de la Serie A.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error en Serie A: " + e.getMessage());
        }
    }

    @PostMapping("/bundesliga")
    public ResponseEntity<String> procesarBundesliga() {
        try {
            playerSimulationService.procesarSoloBundesliga();
            return ResponseEntity.ok("Éxito: Se han limpiado las simulaciones de la Bundesliga.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error en Bundesliga: " + e.getMessage());
        }
    }

    @PostMapping("/ligue1")
    public ResponseEntity<String> procesarLigue1() {
        try {
            playerSimulationService.procesarSoloLigue1();
            return ResponseEntity.ok("Éxito: Se han limpiado las simulaciones de la Ligue 1.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error en Ligue 1: " + e.getMessage());
        }
    }
}