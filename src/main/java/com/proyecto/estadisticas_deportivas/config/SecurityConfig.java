package com.proyecto.estadisticas_deportivas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/users/register").permitAll() 
                .requestMatchers("/api/players/search/**").permitAll()
                .requestMatchers("/api/players/basic/**").permitAll() 
                
                // PRIVADO: Lo que requiere estar logueado
                .requestMatchers("/api/stats/compare/**").authenticated() 
                .requestMatchers("/api/favorites/**").authenticated() 
                
                .anyRequest().permitAll() 
            )
            .formLogin(withDefaults()) 
            .httpBasic(withDefaults()); 
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}