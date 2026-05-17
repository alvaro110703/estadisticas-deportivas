package com.proyecto.estadisticas_deportivas.repository;

import com.proyecto.estadisticas_deportivas.model.Player;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepo extends JpaRepository<Player, Long> {

    List<Player> findByTeam(String team);
    List<Player> findByNameContainingIgnoreCase(String name);
    Optional<Player> findByApiPlayerId(String apiPlayerId);
} 
