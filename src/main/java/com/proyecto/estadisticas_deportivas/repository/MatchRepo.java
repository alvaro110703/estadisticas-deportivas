package com.proyecto.estadisticas_deportivas.repository;

import com.proyecto.estadisticas_deportivas.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepo extends JpaRepository<Match, Long> {

    Optional<Match> findByHomeTeamAndAwayTeamAndDate(String homeTeam, String awayTeam, LocalDate date);
    
    List<Match> findByHomeTeamOrAwayTeam(String home, String away);

    List<Match> findByBigMatchTrue();

    List<Match> findByAwayTeam(String teamName);

    List<Match> findByHomeTeam(String teamName);

    Optional<Match> findByApiMatchId(String apiMatchId);

    List<Match> findByDateBetween(LocalDate start, LocalDate end);
}