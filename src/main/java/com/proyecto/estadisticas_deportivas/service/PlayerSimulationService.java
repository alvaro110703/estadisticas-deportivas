package com.proyecto.estadisticas_deportivas.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.estadisticas_deportivas.model.Player;
import com.proyecto.estadisticas_deportivas.repository.PlayerRepo;

import java.util.List;
import java.util.Random;

@Service
public class PlayerSimulationService {

    @Autowired
    private PlayerRepo playerRepository;

    @Transactional
    public void repartirYLimpiarSimulaciones(String teamName) {
        // 1. Obtener todos los jugadores del club
        List<Player> allPlayers = playerRepository.findByTeam(teamName);

        int golesSimulados = 0;
        int asistenciasSimuladas = 0;
        Player delanteroSimulado = null;
        Player medioSimulado = null;

        // 2. Localizar los registros "SIM" y extraer sus datos
        for (Player p : allPlayers) {
            if (p.getApiPlayerId().startsWith("SIM_GOAL_")) {
                golesSimulados = p.getTotalGoals();
                delanteroSimulado = p;
            } else if (p.getApiPlayerId().startsWith("SIM_ASSIST_")) {
                asistenciasSimuladas = p.getTotalAssists();
                medioSimulado = p;
            }
        }

        // Si no hay datos simulados que procesar, salimos del método
        if (delanteroSimulado == null && medioSimulado == null) {
            return;
        }

        // 3. Quedarnos únicamente con los jugadores reales del equipo
        allPlayers.remove(delanteroSimulado);
        allPlayers.remove(medioSimulado);

        if (allPlayers.isEmpty())
            return; // Evitar división por cero si no hay jugadores reales

        Random random = new Random();

        // 4. REPARTIR LOS GOLES SIMULADOS
        for (int i = 0; i < golesSimulados; i++) {
            Player suertudo = seleccionarJugadorPorPesos(allPlayers, "GOAL", random);
            suertudo.setTotalGoals(suertudo.getTotalGoals() + 1);
        }

        // 5. REPARTIR LAS ASISTENCIAS SIMULADAS
        for (int i = 0; i < asistenciasSimuladas; i++) {
            Player suertudo = seleccionarJugadorPorPesos(allPlayers, "ASSIST", random);
            suertudo.setTotalAssists(suertudo.getTotalAssists() + 1);
        }

        // 6. Guardar los jugadores reales con sus nuevas estadísticas actualizadas
        playerRepository.saveAll(allPlayers);

        // 7. Borrar de la base de datos las dos entradas "SIM" viejas
        if (delanteroSimulado != null)
            playerRepository.delete(delanteroSimulado);
        if (medioSimulado != null)
            playerRepository.delete(medioSimulado);
    }

    // Método auxiliar para dar más "papeletas de lotería" según la posición del
    // jugador
    private Player seleccionarJugadorPorPesos(List<Player> jugadores, String tipoMétrica, Random random) {
        while (true) {
            Player p = jugadores.get(random.nextInt(jugadores.size()));
            int probabilidad = random.nextInt(100); // Número de 0 a 99

            if (tipoMétrica.equals("GOAL")) {
                // Los delanteros se quedan con el gol el 70% de las veces que salen elegidos
                if (p.getPosition().equals("ATTACKER") && probabilidad < 70)
                    return p;
                // Los centrocampistas el 25% de las veces
                if (p.getPosition().equals("MIDFIELDER") && probabilidad < 25)
                    return p;
                // Los defensas solo el 5% de las veces
                if (p.getPosition().equals("DEFENDER") && probabilidad < 5)
                    return p;
            } else { // "ASSIST"
                // Las asistencias van al mediocampo el 60% de las veces
                if (p.getPosition().equals("MIDFIELDER") && probabilidad < 60)
                    return p;
                // A los delanteros el 30% de las veces
                if (p.getPosition().equals("ATTACKER") && probabilidad < 30)
                    return p;
                // A los defensas el 10% de las veces
                if (p.getPosition().equals("DEFENDER") && probabilidad < 10)
                    return p;
            }
        }
    }

    @Transactional
    public void procesarSoloLigaEspanola() {
        // Tu lista exacta con los 20 clubes tal y como están en tu base de datos
        List<String> equiposLaLiga = List.of(
                "Rayo Vallecano",
                "Girona",
                "Real Oviedo",
                "Villarreal",
                "Deportivo Alavés",
                "Levante",
                "Barcelona",
                "Valencia",
                "Real Sociedad",
                "Athletic Bilbao",
                "Sevilla",
                "Getafe",
                "Atlético Madrid",
                "Espanyol",
                "Real Betis",
                "Elche",
                "Real Madrid",
                "Celta Vigo",
                "Mallorca",
                "Osasuna");

        // El bucle va mirando uno a uno tus equipos de la lista
        for (String equipo : equiposLaLiga) {
            repartirYLimpiarSimulaciones(equipo);
        }
    }

    @Transactional
    public void procesarSoloPremierLeague() {
        List<String> equiposPremier = List.of(
                "Liverpool", "Bournemouth", "Brighton and Hove Albion", "Fulham",
                "Sunderland", "Nottingham Forest", "Brentford", "Arsenal",
                "Leeds United", "West Ham United", "Chelsea", "Crystal Palace",
                "Everton", "Manchester United", "Newcastle United", "Burnley",
                "Manchester City", "Aston Villa", "Tottenham Hotspur", "Wolverhampton Wanderers");

        for (String equipo : equiposPremier) {
            repartirYLimpiarSimulaciones(equipo);
        }
    }

    @Transactional
    public void procesarSoloSerieA() {
        List<String> equiposSerieA = List.of(
                "Cremonese", "AC Milan", "Roma", "Pisa", "Atalanta",
                "Cagliari", "Fiorentina", "Como", "Inter Milan", "Udinese",
                "Hellas Verona", "Sassuolo", "Bologna", "Napoli", "Parma",
                "Juventus", "Lazio", "Lecce", "Genoa", "Torino");

        for (String equipo : equiposSerieA) {
            repartirYLimpiarSimulaciones(equipo);
        }
    }

    @Transactional
    public void procesarSoloBundesliga() {
        List<String> equiposBundesliga = List.of(
                "Bayern Munich", "Bayer Leverkusen", "Hoffenheim", "Eintracht Frankfurt",
                "Werder Bremen", "FC Augsburg", "Freiburg", "FC Köln", "St Pauli",
                "RB Leipzig", "Borussia Dortmund", "Wolfsburg", "Mainz", "Stuttgart",
                "FC Heidenheim", "Union Berlin", "Hamburg", "Borussia Mönchengladbach");

        for (String equipo : equiposBundesliga) {
            repartirYLimpiarSimulaciones(equipo);
        }
    }

    @Transactional
    public void procesarSoloLigue1() {
        // Lista exacta con todos los equipos franceses de tu conjunto
        List<String> equiposLigue1 = List.of(
                "Rennes", "Lyon", "Monaco", "Le Havre", "Toulouse",
                "Angers", "Auxerre", "Lille", "Brest", "Paris SG",
                "Marseille", "Paris FC", "Nice", "Lens", "Lorient",
                "Nantes", "Metz", "Strasbourg", "Red Star", "Rodez AF");

        for (String equipo : equiposLigue1) {
            repartirYLimpiarSimulaciones(equipo);
        }
    }

}