package com.proyecto.estadisticas_deportivas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        // Mantenemos el Bean de CORS para que React (5173) pueda hablar con Java (8080)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        
        .authorizeHttpRequests(auth -> auth
            // 🌟 AÑADIMOS LAS RUTAS DE FAVORITOS AQUÍ (LIBRES)
            .requestMatchers("/api/users/register").permitAll() 
            .requestMatchers("/api/players/search/**").permitAll()
            .requestMatchers("/api/players/basic/**").permitAll() 
            .requestMatchers("/api/favorites/**").permitAll() // 👈 ¡Libertad total!
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            
            // Solo dejamos bajo llave la comparación si quieres, o lo que desees probar después
            .requestMatchers("/api/stats/compare/**").authenticated() 
            
            .anyRequest().permitAll() 
        )
        .formLogin(withDefaults()) 
        .httpBasic(basic -> basic
            .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
        ); 
    
    return http.build();
}

    // 4. Bean de CORS global obligado para peticiones seguras/con credenciales entre puertos 5173 y 8080
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Dirección exacta de tu Vite/React
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173")); 
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Cabeceras permitidas clave para procesar Basic Auth y envíos JSON
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        // Clave para que acepte tus peticiones con `credentials: 'include'`
        configuration.setAllowCredentials(true); 
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}