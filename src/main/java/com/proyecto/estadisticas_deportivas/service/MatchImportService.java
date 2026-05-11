package com.proyecto.estadisticas_deportivas.service;

import com.proyecto.estadisticas_deportivas.model.Match;
import com.proyecto.estadisticas_deportivas.repository.MatchRepo;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
public class MatchImportService {

    @Autowired
    private MatchRepo matchRepo;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    private boolean checkIsBigMatch(String homeTeam, String awayTeam) {
        List<String> topTeams = Arrays.asList(
                "Real Madrid", "FC Barcelona", "Barcelona", "Atletico Madrid", "Athletic Club",
                "Manchester City", "Liverpool", "Arsenal", "Manchester United", "Chelsea", "Tottenham",
                "Juventus", "Inter", "AC Milan", "Milan", "Napoli",
                "Bayern Munich", "Bayern Muenchen", "Borussia Dortmund", "Bayer Leverkusen",
                "Paris Saint Germain", "PSG", "Marseille", "Lyon"
        );
        return topTeams.contains(homeTeam) || topTeams.contains(awayTeam);
    }

    public void importFullSeasonByDays(String leagueId) {
        LocalDate startDate = LocalDate.of(2026, 5, 1);
        LocalDate endDate = LocalDate.now();

        System.out.println(">>> Iniciando importación masiva para la liga: " + leagueId);

        while (startDate.isBefore(endDate)) {
            String url = "https://www.thesportsdb.com/api/v1/json/3/eventsday.php?d=" + startDate + "&l=" + leagueId;
            
            try {
                String response = restTemplate.getForObject(url, String.class);
                JsonNode root = mapper.readTree(response);
                JsonNode events = root.path("events");

                if (events.isArray() && !events.isEmpty()) {
                    for (JsonNode event : events) {
                        processAndSaveMatch(event);
                    }
                    System.out.println("[OK] Datos guardados para el día: " + startDate);
                } else {
                    System.out.println("[INFO] Sin partidos el día: " + startDate);
                }
                
                // Pausa de seguridad para evitar el error 429 (Too Many Requests)
                Thread.sleep(3000); 
                
            } catch (Exception e) {
                if (e.getMessage().contains("429")) {
                    System.err.println("!!! Límite de API alcanzado. Esperando 1 minuto para reintentar...");
                    try { Thread.sleep(60000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    continue; 
                } else {
                    System.err.println("[ERROR] Día " + startDate + ": " + e.getMessage());
                }
            }
            startDate = startDate.plusDays(1);
        }
        System.out.println(">>> Proceso finalizado para la liga " + leagueId);
    }

    private void processAndSaveMatch(JsonNode event) {
        String home = event.path("strHomeTeam").asText();
        String away = event.path("strAwayTeam").asText();
        LocalDate date = LocalDate.parse(event.path("dateEvent").asText());

        if (matchRepo.findByHomeTeamAndAwayTeamAndDate(home, away, date).isEmpty()) {
            String hScoreStr = event.path("intHomeScore").asText();
            String aScoreStr = event.path("intAwayScore").asText();

            if (!hScoreStr.equals("null") && !hScoreStr.isEmpty()) {
                Match match = new Match(
                    home, away, 
                    Integer.parseInt(hScoreStr), 
                    Integer.parseInt(aScoreStr), 
                    date, 
                    checkIsBigMatch(home, away)
                );
                matchRepo.save(match);
            }
        }
    }
}