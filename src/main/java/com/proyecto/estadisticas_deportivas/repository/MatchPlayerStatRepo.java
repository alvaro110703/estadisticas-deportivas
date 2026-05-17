package com.proyecto.estadisticas_deportivas.repository;

import com.proyecto.estadisticas_deportivas.model.MatchPlayerStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchPlayerStatRepo extends JpaRepository<MatchPlayerStat, Long> {

    // Buscar estadísticas filtrando si es local (isHome) o visitante
    @Query("SELECT s FROM MatchPlayerStat s WHERE s.player.id = :playerId AND s.isHome = :isHome")
    List<MatchPlayerStat> findByPlayerIdAndIsHome(@Param("playerId") Long playerId, @Param("isHome") boolean isHome);
}