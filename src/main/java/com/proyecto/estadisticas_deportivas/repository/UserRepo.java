package com.proyecto.estadisticas_deportivas.repository;

import com.proyecto.estadisticas_deportivas.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    Optional<User> findByCorreo(String correo);
    Optional<User> findByNombreOrCorreo(String nombre, String correo);
}