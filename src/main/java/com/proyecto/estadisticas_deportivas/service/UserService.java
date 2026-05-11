package com.proyecto.estadisticas_deportivas.service;

import com.proyecto.estadisticas_deportivas.model.Player;
import com.proyecto.estadisticas_deportivas.model.User;
import com.proyecto.estadisticas_deportivas.repository.PlayerRepo;
import com.proyecto.estadisticas_deportivas.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PlayerRepo playerRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registrarUsuario(User user) {
        if (userRepo.findByCorreo(user.getCorreo()).isPresent()) {
            throw new RuntimeException("El correo ya está registrado");
        }

        user.setContrasena(passwordEncoder.encode(user.getContrasena()));

        if (user.getRol() == null || user.getRol().isEmpty()) {
            user.setRol("USER");
        }

        return userRepo.save(user);
    }

    public void addFavorite(Long userId, Player playerFromApi) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!playerRepo.existsById(playerFromApi.getId())) {
            playerRepo.save(playerFromApi);
        }

        user.getFavoritePlayers().add(playerFromApi);
        userRepo.save(user);
    }

    public void removeFavorite(Long userId, String playerId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.getFavoritePlayers().removeIf(p -> p.getId().equals(playerId));
        userRepo.save(user);
    }

    public boolean isFavorite(Long userId, String playerId) {
        Optional<User> userOpt = userRepo.findById(userId);
        if (userOpt.isEmpty()) return false;

        return userOpt.get().getFavoritePlayers().stream()
                .anyMatch(p -> p.getId().equals(playerId));
    }

    public Set<Player> getFavorites(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return user.getFavoritePlayers();
    }
}