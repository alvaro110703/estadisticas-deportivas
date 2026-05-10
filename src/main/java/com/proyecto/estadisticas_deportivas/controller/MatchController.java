package com.proyecto.estadisticas_deportivas.controller;

import com.proyecto.estadisticas_deportivas.service.MatchImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    @Autowired
    private MatchImportService matchImportService;

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
    public String importAll() {
        String[] bigLeagues = {"4335", "4328", "4332", "4331", "4334"};
        for (String id : bigLeagues) {
            matchImportService.importFullSeasonByDays(id);
        }
        return "Importación de todas las grandes ligas iniciada.";
    }
}