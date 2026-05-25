package com.proyecto.estadisticas_deportivas.service;

import com.proyecto.estadisticas_deportivas.model.Player;
import com.proyecto.estadisticas_deportivas.model.User;
import com.proyecto.estadisticas_deportivas.repository.PlayerRepo;
import com.proyecto.estadisticas_deportivas.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetailsService;
import java.util.Collections;

import java.util.Optional;
import java.util.Set;

@Service
public class UserService implements UserDetailsService {

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
        if (userOpt.isEmpty())
            return false;

        return userOpt.get().getFavoritePlayers().stream()
                .anyMatch(p -> p.getId().equals(playerId));
    }

    public Set<Player> getFavoritesByEmail(String email) {
        // Necesitas tener un método findByCorreo en tu userRepo
        User user = userRepo.findByCorreo(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));
        return user.getFavoritePlayers();
    }

    public Set<Player> getFavoritesByUserId(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));
        return user.getFavoritePlayers();
    }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        User user = userRepo.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con correo: " + correo));

        // Mapeamos tu usuario de la DB al formato que entiende Spring Security
        return new org.springframework.security.core.userdetails.User(
                user.getCorreo(),
                user.getContrasena(), // Aquí va la clave que ya guardaste encriptada en el POST
                Collections.emptyList() // Sin roles por ahora para no complicarlo
        );
    }
}