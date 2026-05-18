package com.proyecto.estadisticas_deportivas.repository;

import com.proyecto.estadisticas_deportivas.model.MatchEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchEventRepo extends JpaRepository<MatchEvent, Long> {
    // De momento con los métodos heredados de JpaRepository (como .save()) nos basta
    boolean existsByMatchId(String matchId);
}