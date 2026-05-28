package com.proyecto.estadisticas_deportivas.controller;

import com.proyecto.estadisticas_deportivas.model.Match;
import com.proyecto.estadisticas_deportivas.repository.MatchRepo;
import com.proyecto.estadisticas_deportivas.service.MatchImportService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = "http://localhost:5173")
public class MatchController {

    @Autowired
    private MatchImportService matchImportService;

    @Autowired
    private MatchRepo matchRepo;

    @GetMapping("/import-full/{leagueId}")
    public String importFullSeason(@PathVariable String leagueId) {
        try {
            matchImportService.importFullSeasonByDays(leagueId);
            return "Importación completa iniciada para la liga: " + leagueId;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/import-all")
    public ResponseEntity<List<MatchImportService.PlayerDebugInfo>> importAll() {
        // 1. Antes de empezar los bucles, le decimos al servicio que limpie los datos
        // antiguos
        matchImportService.inicializarListaDebug();

        String[] bigLeagues = { "4335", "4328", "4332", "4331", "4334" };
        for (String id : bigLeagues) {
            matchImportService.importFullSeasonByDays(id);
        }

        // 2. Recuperamos la lista global que ha ido acumulando todos los jugadores de
        // todas las ligas
        List<MatchImportService.PlayerDebugInfo> jugadoresGuardados = matchImportService.getDebugList();

        // 3. Devolvemos el JSON reluciente en Postman
        return ResponseEntity.ok(jugadoresGuardados);
    }

    @GetMapping("/importar-eventos-pendientes")
    public ResponseEntity<List<MatchImportService.PlayerDebugInfo>> importarEventosPendientes() {
        System.out.println(">>> Recibida petición limpia para procesar eventos pendientes...");

        // 1. Limpiamos la lista de diagnóstico para ver solo los datos de esta tanda
        matchImportService.inicializarListaDebug();

        // 2. Lanzamos el proceso automatizado con el rango de fechas interno
        matchImportService.procesarEventosPorRangoFijo();

        // 3. Devolvemos el JSON de feedback en Postman
        return ResponseEntity.ok(matchImportService.getDebugList());
    }

    @GetMapping("/importar-posiciones-jugadores")
    public ResponseEntity<List<MatchImportService.PlayerDebugInfo>> importarPosicionesJugadores() {
        // Lanzamos la fase 3 de reparación asíncrona
        matchImportService.repararPosicionesUnknown();

        // Devolvemos la lista de los jugadores que se han corregido con su posición
        // real
        return ResponseEntity.ok(matchImportService.getDebugList());
    }

    @GetMapping("/reparar-dias-diez")
    public ResponseEntity<String> repararDiasDiez() {
        System.out.println(">>> Disparando el parcheador de seguridad para los días 10...");

        // Ejecuta el script que acabamos de crear
        matchImportService.repararDiasDiezFaltantes();

        return ResponseEntity.ok("Proceso de reparación finalizado con éxito. Revisa la consola del IDE.");
    }

    @GetMapping("/recent")
    public ResponseEntity<List<java.util.Map<String, String>>> getRecentMatches(
            @org.springframework.web.bind.annotation.RequestParam String team) {
        // Buscamos todos los partidos de ese club (local o visitante)
        List<Match> todosLosPartidos = matchRepo.findByHomeTeamOrAwayTeam(team, team);

        // Los ordenamos por fecha de más reciente a más antiguo
        todosLosPartidos.sort((m1, m2) -> m2.getDate().compareTo(m1.getDate()));

        // Nos quedamos con un máximo de 5 partidos
        List<Match> ultimos5 = todosLosPartidos.stream().limit(5).collect(java.util.stream.Collectors.toList());

        // Mapeamos los partidos al formato de texto simplificado que espera tu Frontend
        // ("RM 3 - 1 SEV")
        List<java.util.Map<String, String>> respuestaFront = new java.util.ArrayList<>();

        for (Match m : ultimos5) {
            java.util.Map<String, String> datosPartido = new java.util.HashMap<>();

            // Suponiendo que tus atributos en la entidad Match se llaman getHomeTeam(),
            // getAwayTeam()
            // y que tienes los goles guardados como getHomeScore() y getAwayScore() (o
            // similar)
            String textoPartido = m.getHomeTeam() + " " + m.getHomeScore() + " - " + m.getAwayScore() + " "
                    + m.getAwayTeam();

            datosPartido.put("texto", textoPartido);
            respuestaFront.add(datosPartido);
        }

        return ResponseEntity.ok(respuestaFront);
    }

    @GetMapping("/total-count")
    public ResponseEntity<Integer> getTotalMatchesCount(@RequestParam String team) {
        // Obtenemos la lista completa de encuentros registrados en la BD para este club
        List<Match> todosLosPartidos = matchRepo.findByHomeTeamOrAwayTeam(team, team);
        
        // Devolvemos simplemente el tamaño de la lista (el total acumulado)
        if (todosLosPartidos != null) {
            return ResponseEntity.ok(todosLosPartidos.size());
        }
        return ResponseEntity.ok(0);
    }
}