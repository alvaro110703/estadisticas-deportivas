package com.proyecto.estadisticas_deportivas.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String homeTeam;
    private String awayTeam;
    private int homeScore;
    private int awayScore;
    
    private LocalDate date;

    private boolean bigMatch;
    private String apiMatchId;
    public Match() {}

    public Match(String homeTeam, String awayTeam, int homeScore, int awayScore, LocalDate date, boolean bigMatch, String apiMatchId) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.date = date;
        this.bigMatch = bigMatch;
        this.apiMatchId = apiMatchId;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getHomeTeam() { return homeTeam; }
    public void setHomeTeam(String homeTeam) { this.homeTeam = homeTeam; }

    public String getAwayTeam() { return awayTeam; }
    public void setAwayTeam(String awayTeam) { this.awayTeam = awayTeam; }

    public int getHomeScore() { return homeScore; }
    public void setHomeScore(int homeScore) { this.homeScore = homeScore; }

    public int getAwayScore() { return awayScore; }
    public void setAwayScore(int awayScore) { this.awayScore = awayScore; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public boolean isBigMatch() { return bigMatch; }
    public void setBigMatch(boolean bigMatch) { this.bigMatch = bigMatch; }

    public String getApiMatchId() {
    return apiMatchId;
}

public void setApiMatchId(String apiMatchId) {
    this.apiMatchId = apiMatchId;
}
}
