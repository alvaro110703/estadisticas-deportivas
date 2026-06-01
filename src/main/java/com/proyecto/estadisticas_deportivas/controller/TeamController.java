package com.proyecto.estadisticas_deportivas.controller;

import com.proyecto.estadisticas_deportivas.model.Match;
import com.proyecto.estadisticas_deportivas.repository.MatchRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin(origins = "http://localhost:5173")
public class TeamController {

    @Autowired
    private MatchRepo matchRepository;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> obtenerClubesConEstadisticas() {
        List<Match> partidos = matchRepository.findAll();
        Map<String, Map<String, Object>> estadisticasPorClub = new TreeMap<>();

        for (Match partido : partidos) {
            String local = partido.getHomeTeam();
            String visitante = partido.getAwayTeam();

            if (local == null || visitante == null)
                continue;

            int golesLocal = partido.getHomeScore();
            int golesVisitante = partido.getAwayScore();

            inicializarClub(estadisticasPorClub, local);
            inicializarClub(estadisticasPorClub, visitante);

            Map<String, Object> statsLocal = estadisticasPorClub.get(local);
            Map<String, Object> statsVisitante = estadisticasPorClub.get(visitante);

            statsLocal.put("golesFavor", (int) statsLocal.get("golesFavor") + golesLocal);
            statsLocal.put("golesContra", (int) statsLocal.get("golesContra") + golesVisitante);

            statsVisitante.put("golesFavor", (int) statsVisitante.get("golesFavor") + golesVisitante);
            statsVisitante.put("golesContra", (int) statsVisitante.get("golesContra") + golesLocal);

            if (golesLocal > golesVisitante) {
                statsLocal.put("victorias", (int) statsLocal.get("victorias") + 1);
                statsVisitante.put("derrotas", (int) statsVisitante.get("derrotas") + 1);
            } else if (golesLocal < golesVisitante) {
                statsVisitante.put("victorias", (int) statsVisitante.get("victorias") + 1);
                statsLocal.put("derrotas", (int) statsLocal.get("derrotas") + 1);
            } else {
                statsLocal.put("empates", (int) statsLocal.get("empates") + 1);
                statsVisitante.put("empates", (int) statsVisitante.get("empates") + 1);
            }
        }

        // =========================================================================
        // 🌟 LISTAS OFICIALES Y COMPLETAS DE LAS 5 GRANDES LIGAS (EN MINÚSCULAS) 🌟
        // =========================================================================

        List<String> laLiga = List.of(
                "rayo vallecano",
                "girona",
                "real oviedo",
                "villarreal",
                "deportivo alaves",
                "levante",
                "barcelona",
                "valencia",
                "real sociedad",
                "athletic bilbao",
                "sevilla",
                "getafe",
                "atletico madrid",
                "espanyol",
                "real betis",
                "elche",
                "real madrid",
                "celta vigo",
                "mallorca",
                "osasuna");

        List<String> premierLeague = List.of(
                "arsenal", "aston villa", "bournemouth", "brentford", "brighton and hove albion",
                "brighton", "burnley", "chelsea", "crystal palace", "everton", "fulham",
                "liverpool", "luton town", "luton", "manchester city", "manchester united",
                "newcastle united", "newcastle", "sheffield united", "tottenham hotspur",
                "tottenham", "west ham united", "west ham", "wolverhampton wanderers", "wolves",
                "sunderland", "leeds united", "nottingham forest");

        List<String> serieA = List.of(
                "ac milan", "milan", "atalanta", "bologna", "cagliari", "como", "empoli",
                "fiorentina", "frosinone", "genoa", "hellas verona", "verona", "inter milan",
                "inter", "juventus", "lazio", "lecce", "monza", "napoli", "roma", "salernitana",
                "sampdoria", "sassuolo", "torino", "udinese", "cremonese", "pisa", "parma");

        List<String> ligue1 = List.of(
                "angers", "auxerre", "brest", "clermont", "le havre", "lens", "lille",
                "lorient", "lyon", "marseille", "metz", "monaco", "montpellier", "nantes",
                "nice", "paris saint germain", "psg", "paris sg", "reims", "rennes", "strasbourg",
                "toulouse", "paris fc", "red star", "rodez af", "st etienne");

        List<String> bundesliga = List.of(
                "augsburg", "fc augsburg", "bayer leverkusen", "bayern munich", "bayern",
                "bochum", "borussia dortmund", "dortmund", "borussia mönchengladbach",
                "borussia monchengladbach", "darmstadt", "eintracht frankfurt", "frankfurt",
                "freiburg", "fc heidenheim", "heidenheim", "hoffenheim", "fc köln", "koeln",
                "koln", "mainz", "rb leipzig", "leipzig", "stuttgart", "werder bremen", "wolfsburg",
                "st pauli", "hamburg", "union berlin", "paderborn");

        // =========================================================================

        List<Map<String, Object>> respuesta = new ArrayList<>();
        int idContador = 1;

        for (Map.Entry<String, Map<String, Object>> entrada : estadisticasPorClub.entrySet()) {
            Map<String, Object> jsonClub = entrada.getValue();
            jsonClub.put("id", (long) idContador++);

            String nombreReal = entrada.getKey();
            jsonClub.put("name", nombreReal);

            // Limpieza de espacios y acentos básicos para el match
            String nombreMinuscula = nombreReal.toLowerCase()
                    .replace("ó", "o")
                    .replace("é", "e")
                    .replace("á", "a")
                    .replace("í", "i")
                    .replace("ú", "u")
                    .trim();

            // Marcador por defecto exigido
            String codigoLiga = "NO_ASIGNADO";

            if (laLiga.contains(nombreMinuscula)) {
                codigoLiga = "PD";
            } else if (premierLeague.contains(nombreMinuscula)) {
                codigoLiga = "PL";
            } else if (serieA.contains(nombreMinuscula)) {
                codigoLiga = "SA";
            } else if (ligue1.contains(nombreMinuscula)) {
                codigoLiga = "FL1";
            } else if (bundesliga.contains(nombreMinuscula)) {
                codigoLiga = "BL1";
            }

            jsonClub.put("competitionCode", codigoLiga);
            respuesta.add(jsonClub);
        }

        return ResponseEntity.ok(respuesta);
    }

    private void inicializarClub(Map<String, Map<String, Object>> mapa, String nombreClub) {
        if (!mapa.containsKey(nombreClub)) {
            Map<String, Object> datosIniciales = new HashMap<>();
            datosIniciales.put("victorias", 0);
            datosIniciales.put("empates", 0);
            datosIniciales.put("derrotas", 0);
            datosIniciales.put("golesFavor", 0);
            datosIniciales.put("golesContra", 0);
            mapa.put(nombreClub, datosIniciales);
        }
    }
}