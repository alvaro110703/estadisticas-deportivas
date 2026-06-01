package com.proyecto.estadisticas_deportivas.service;

import com.proyecto.estadisticas_deportivas.model.Match;
import com.proyecto.estadisticas_deportivas.model.MatchEvent;
import com.proyecto.estadisticas_deportivas.model.Player;
import com.proyecto.estadisticas_deportivas.repository.MatchRepo;
import com.proyecto.estadisticas_deportivas.repository.PlayerRepo;
import com.proyecto.estadisticas_deportivas.repository.MatchEventRepo; // Tu nuevo repositorio

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class MatchImportService {

    @Autowired
    private PlayerRepo playerRepo;

    @Autowired
    private MatchRepo matchRepo;

    @Autowired
    private MatchEventRepo matchEventRepo; // Inyectamos el nuevo repositorio de eventos

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    private boolean checkIsBigMatch(String homeTeam, String awayTeam) {
        List<String> topTeams = Arrays.asList(
                "Real Madrid", "Barcelona", "Atlético Madrid", "Athletic Bilbao",
                "Manchester City", "Liverpool", "Arsenal", "Manchester United", "Chelsea", "Tottenham Hotspur",
                "Juventus", "Inter Milan", "AC Milan", "Napoli", "Roma",
                "Bayern Munich", "Borussia Dortmund", "Bayer Leverkusen",
                "Marseille", "Lyon", "Paris SG");
        return topTeams.contains(homeTeam) || topTeams.contains(awayTeam);
    }

    public void importFullSeasonByDays(String leagueId) {
        LocalDate startDate = LocalDate.of(2026, 5, 16);
        LocalDate endDate = LocalDate.of(2026, 5, 25);

        System.out.println(">>> Iniciando importación masiva para la liga: " + leagueId);

        while (startDate.isBefore(endDate)) {
            String url = "https://www.thesportsdb.com/api/v1/json/3/eventsday.php?d=" + startDate + "&l=" + leagueId;

            try {
                String response = restTemplate.getForObject(url, String.class);
                JsonNode root = mapper.readTree(response);
                JsonNode events = root.path("events");

                if (events.isArray() && !events.isEmpty()) {
                    for (JsonNode event : events) {
                        processAndSaveMatch(event);
                    }
                    System.out.println("[OK] Datos guardados para el día: " + startDate);
                } else {
                    System.out.println("[INFO] Sin partidos el día: " + startDate);
                }

                // Pausa de seguridad para evitar el error 429 (Too Many Requests)
                Thread.sleep(3000);

            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("429")) {
                    System.err.println("!!! Límite de API alcanzado. Esperando 1 minuto para reintentar...");
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                } else {
                    System.err.println("[ERROR] Día " + startDate + ": " + e.getMessage());
                }
            }
            startDate = startDate.plusDays(1);
        }
        System.out.println(">>> Proceso finalizado para la liga " + leagueId);
    }

    public void importarPartidosPorRangoDinamico(LocalDate startDate, LocalDate endDate, String leagueId) {
        System.out.println(">>> Iniciando importación masiva para la liga: " + leagueId);

        // Usamos una variable auxiliar para no perder la fecha de inicio original
        LocalDate currentDate = startDate;

        while (currentDate.isBefore(endDate)) {
            String url = "https://www.thesportsdb.com/api/v1/json/3/eventsday.php?d=" + currentDate + "&l=" + leagueId;

            try {
                String response = restTemplate.getForObject(url, String.class);
                JsonNode root = mapper.readTree(response);
                JsonNode events = root.path("events");

                if (events.isArray() && !events.isEmpty()) {
                    for (JsonNode event : events) {
                        processAndSaveMatch(event);
                    }
                    System.out
                            .println("[OK] Datos guardados para el día: " + currentDate + " (Liga: " + leagueId + ")");
                } else {
                    System.out.println("[INFO] Sin partidos el día: " + currentDate + " (Liga: " + leagueId + ")");
                }

                // Pausa de seguridad para evitar el error 429 (Too Many Requests)
                Thread.sleep(3000);

            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("429")) {
                    System.err.println("!!! Límite de API alcanzado. Esperando 1 minuto para reintentar...");
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    continue; // Reintenta el mismo día
                } else {
                    System.err.println("[ERROR] Día " + currentDate + " - Liga " + leagueId + ": " + e.getMessage());
                }
            }
            // Avanzamos al siguiente día
            currentDate = currentDate.plusDays(1);
        }
        System.out.println(">>> Proceso finalizado para la liga " + leagueId);
    }

    private void processAndSaveMatch(JsonNode event) {
        String home = event.path("strHomeTeam").asText();
        String away = event.path("strAwayTeam").asText();
        LocalDate date = LocalDate.parse(event.path("dateEvent").asText());
        String apiMatchId = event.path("idEvent").asText();

        // Cambiamos a Optional tal cual lo tienes en tu MatchRepo
        Optional<Match> matchOptional = matchRepo.findByHomeTeamAndAwayTeamAndDate(home, away, date);

        if (matchOptional.isEmpty()) {
            // CASO A: El partido es nuevo. Se crea desde cero.
            String hScoreStr = event.path("intHomeScore").asText();
            String aScoreStr = event.path("intAwayScore").asText();

            if (!hScoreStr.equals("null") && !hScoreStr.isEmpty()) {
                Match match = new Match(
                        home, away,
                        Integer.parseInt(hScoreStr),
                        Integer.parseInt(aScoreStr),
                        date,
                        checkIsBigMatch(home, away),
                        apiMatchId);
                matchRepo.save(match);

                // Descargamos los eventos para este partido nuevo
                // importTimelineEvents(apiMatchId);
            }
        } else {
            // CASO B: El partido ya existía. Lo extraemos del Optional.
            Match matchExistente = matchOptional.get();
            matchExistente.setBigMatch(checkIsBigMatch(matchExistente.getHomeTeam(), matchExistente.getAwayTeam()));
            matchExistente.setApiMatchId(apiMatchId); // Le inyectamos el ID que le faltaba
            matchRepo.save(matchExistente); // Hacemos el UPDATE (save) en MySQL

            // Descargamos los eventos por primera vez para este partido existente
            // importTimelineEvents(apiMatchId);
            System.out.println(
                    "   [ACTUALIZADO] Partido existente " + home + " vs " + away + " actualizado con su API ID.");
        }
    }

    public void procesarEventosPendientes() {
        System.out.println(">>> Iniciando sincronización de eventos para partidos pendientes...");

        // 1. Necesitas añadir este método en tu MatchRepo para traer los partidos que
        // guardaste
        List<Match> todosLosPartidos = matchRepo.findAll();

        for (Match partido : todosLosPartidos) {
            String apiMatchId = partido.getApiMatchId();

            if (apiMatchId != null && !apiMatchId.isEmpty()) {
                // Comprobamos si ya le habíamos bajado los eventos para no repetir peticiones
                boolean yaTieneEventos = matchEventRepo.existsByMatchId(apiMatchId);

                if (!yaTieneEventos) {
                    System.out.println("[TIMELINE] Descargando eventos del partido ID: " + apiMatchId);

                    // Llamamos a tu método actual que descarga, simula y guarda todo
                    importTimelineEvents(apiMatchId);

                    // ⏱️ PAUSA QUIRÚRGICA: Esperamos 4 segundos entre partido y partido
                    // Esto distribuye las peticiones en el tiempo y evita el baneo por completo
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        System.out.println(">>> Sincronización de eventos finalizada.");
    }

    public void procesarEventosPorRangoFijo() {
        // 🎯 CONFIGURACIÓN MANUAL DE FECHAS: Modifica estas líneas según el mes que
        // vayas a procesar
        LocalDate startDate = LocalDate.of(2026, 5, 13);
        LocalDate endDate = LocalDate.of(2026, 5, 25);

        System.out.println(">>> Sincronizando eventos para partidos en el rango fijo: " + startDate + " a " + endDate);

        // 1. Buscamos en el repositorio los partidos locales que caigan en esta franja
        // de fechas
        List<Match> partidosDelRango = matchRepo.findByDateBetween(startDate, endDate);

        System.out.println("[INFO] Se han encontrado " + partidosDelRango.size()
                + " partidos en la base de datos para este rango.");

        for (Match partido : partidosDelRango) {
            String apiMatchId = partido.getApiMatchId();

            if (apiMatchId != null && !apiMatchId.isEmpty()) {
                // 2. Comprobamos si ya tiene eventos para no duplicar peticiones en caso de
                // reintentos
                boolean yaTieneEventos = matchEventRepo.existsByMatchId(apiMatchId);

                if (!yaTieneEventos) {
                    System.out.println("[TIMELINE] Descargando eventos del partido: "
                            + partido.getHomeTeam() + " vs " + partido.getAwayTeam() + " (ID: " + apiMatchId + ")");

                    // Descarga e inventa los eventos si faltan (con asistencia en el mismo minuto)
                    importTimelineEvents(apiMatchId);

                    // ⏱️ PAUSA QUIRÚRGICA: 4 segundos para respetar el Rate Limit de la API
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        System.out.println(">>> Sincronización de eventos finalizada para el rango fijo.");
    }

    /**
     * Descarga los eventos detallados (goles y asistencias) usando la
     * infraestructura existente
     */
    private void importTimelineEvents(String apiMatchId) {
        String urlTimeline = "https://www.thesportsdb.com/api/v1/json/123/lookuptimeline.php?id=" + apiMatchId;

        try {
            // 1. Buscamos el partido en nuestra DB local para saber el resultado real que
            // tú ya tenías guardado
            Optional<Match> matchOpt = matchRepo.findByApiMatchId(apiMatchId);
            if (matchOpt.isEmpty())
                return;
            Match matchLocal = matchOpt.get();

            int golesHomeReales = matchLocal.getHomeScore();
            int golesAwayReales = matchLocal.getAwayScore();

            String responseStr = restTemplate.getForObject(urlTimeline, String.class);
            JsonNode root = mapper.readTree(responseStr);
            JsonNode timelineArray = root.path("timeline");

            int golesHomeDescargados = 0;
            int golesAwayDescargados = 0;

            if (timelineArray.isArray() && !timelineArray.isEmpty()) {
                for (JsonNode node : timelineArray) {
                    String type = node.path("strTimeline").asText();

                    // === DENTRO DEL BUCLE DE GOLES ===
                    if ("Goal".equalsIgnoreCase(type)) {
                        String playerId = node.path("idPlayer").asText();
                        String playerName = node.path("strPlayer").asText();
                        String teamName = node.path("strTeam").asText(); // Captura el equipo del evento
                        boolean isHome = "Yes".equalsIgnoreCase(node.path("strHome").asText());

                        // Contabilizamos el gol descargado para el equipo que corresponda
                        if (isHome) {
                            golesHomeDescargados++;
                        } else {
                            golesAwayDescargados++;
                        }

                        // Guardamos el evento (Tu lógica actual)
                        MatchEvent goalEvent = new MatchEvent();
                        goalEvent.setMatchId(apiMatchId);
                        goalEvent.setEventType("Goal");
                        goalEvent.setPlayerName(playerName);
                        goalEvent.setApiPlayerId(playerId);
                        goalEvent.setMinute(node.path("intTime").asInt());
                        goalEvent.setHomeEvent(isHome);
                        matchEventRepo.save(goalEvent);

                        // LLAMADA ACTUALIZADA: Pasamos ID, Nombre, Tipo y Equipo
                        actualizarEstadisticasJugador(playerId, playerName, "Goal", teamName);

                        // === DENTRO DE LA ASISTENCIA ===
                        String assistantName = node.path("strAssist").asText();
                        String assistantId = node.path("idAssist").asText();

                        if (assistantName != null && !assistantName.isEmpty() && !assistantName.equals("null")
                                && !assistantId.equals("0")) {
                            MatchEvent assistEvent = new MatchEvent();
                            assistEvent.setMatchId(apiMatchId);
                            assistEvent.setEventType("Assist");
                            assistEvent.setPlayerName(assistantName);
                            assistEvent.setApiPlayerId(assistantId);
                            assistEvent.setMinute(node.path("intTime").asInt());
                            assistEvent.setHomeEvent(isHome);
                            matchEventRepo.save(assistEvent);

                            // LLAMADA ACTUALIZADA: El asistente pertenece al mismo equipo que metió el gol
                            actualizarEstadisticasJugador(assistantId, assistantName, "Assist", teamName);
                        }
                    }
                }
            }

            // === ¡EL PARCHE DE RESPALDO REESTRUCTURADO CON MINUTOS IDÉNTICOS Y AZAR! ===

            // Faltan goles del Local
            if (golesHomeDescargados < golesHomeReales) {
                int golesFaltantes = golesHomeReales - golesHomeDescargados;
                for (int i = 0; i < golesFaltantes; i++) {
                    // 1. Fijamos un único minuto aleatorio para la jugada del gol (segunda parte)
                    int minutoAleatorio = 70 + (int) (Math.random() * 20);

                    // 2. Simulamos el Gol local
                    generarEventoSimulado(apiMatchId, true, "Goal", matchLocal.getHomeTeam(), minutoAleatorio);

                    // 3. Probabilidad aleatoria del 65% para ver si este gol simulado lleva
                    // asistencia
                    if (Math.random() < 0.65) {
                        // 4. Si sale positivo, le inyectamos EXACTAMENTE el mismo minuto del gol
                        generarEventoSimulado(apiMatchId, true, "Assist", matchLocal.getHomeTeam(), minutoAleatorio);
                    }
                }
            }

            // Faltan goles del Visitante
            if (golesAwayDescargados < golesAwayReales) {
                int golesFaltantes = golesAwayReales - golesAwayDescargados;
                for (int i = 0; i < golesFaltantes; i++) {
                    // 1. Fijamos un único minuto aleatorio para la jugada del gol (segunda parte)
                    int minutoAleatorio = 70 + (int) (Math.random() * 20);

                    // 2. Simulamos el Gol visitante
                    generarEventoSimulado(apiMatchId, false, "Goal", matchLocal.getAwayTeam(), minutoAleatorio);

                    // 3. Probabilidad aleatoria del 65% para la asistencia simulada del visitante
                    if (Math.random() < 0.65) {
                        // 4. Si sale positivo, comparte el mismo minuto de juego
                        generarEventoSimulado(apiMatchId, false, "Assist", matchLocal.getAwayTeam(), minutoAleatorio);
                    }
                }
            }

            System.out.println("   [EVENTOS COMPLETADOS] Partido " + apiMatchId
                    + " sincronizado (Reales + Simulados de respaldo).");

        } catch (Exception e) {
            System.err.println("   [ERROR EVENTOS] " + e.getMessage());
            e.printStackTrace(); // Agregado para que no oculte errores de casteo o lógica interna
        }
    }

    /**
     * Método auxiliar para inventar los goles que la API gratuita nos ha ocultado
     */
    private void generarEventoSimulado(String apiMatchId, boolean isHome, String type, String teamName, int minute) {
        MatchEvent simEvent = new MatchEvent();
        simEvent.setMatchId(apiMatchId);
        simEvent.setEventType(type);
        simEvent.setHomeEvent(isHome);

        // 🎯 Ahora el minuto viene fijado de forma consistente desde fuera
        simEvent.setMinute(minute);

        // Asignamos un jugador genérico del equipo
        if ("Goal".equals(type)) {
            simEvent.setPlayerName("Delantero " + teamName);
            simEvent.setApiPlayerId("SIM_GOAL_" + teamName.replaceAll("\\s+", ""));
        } else {
            simEvent.setPlayerName("Centrocampista " + teamName);
            simEvent.setApiPlayerId("SIM_ASSIST_" + teamName.replaceAll("\\s+", ""));
        }

        matchEventRepo.save(simEvent);

        // Actualizamos las estadísticas globales de los contadores de jugadores
        actualizarEstadisticasJugador(simEvent.getApiPlayerId(), simEvent.getPlayerName(), type, teamName);
    }

    private void actualizarEstadisticasJugador(String apiPlayerId, String playerName, String tipoEvento,
            String teamName) {
        Optional<Player> playerOpt = playerRepo.findByApiPlayerId(apiPlayerId);

        Player player;

        if (playerOpt.isEmpty()) {
            player = new Player();
            player.setApiPlayerId(apiPlayerId);
            player.setName(playerName);
            player.setTeam(teamName);
            player.setTotalGoals(0);
            player.setTotalAssists(0);
            // 🎯 OJO: Ya no llamamos a la API aquí. Lo dejamos en UNKNOWN temporalmente.
            player.setPosition("UNKNOWN");
        } else {
            player = playerOpt.get();
        }

        if ("Goal".equals(tipoEvento)) {
            player.setTotalGoals(player.getTotalGoals() + 1);
        } else if ("Assist".equals(tipoEvento)) {
            player.setTotalAssists(player.getTotalAssists() + 1);
        }

        playerRepo.save(player);

        // Guardamos en la lista debug para Postman
        this.debugList
                .add(new PlayerDebugInfo(playerName, apiPlayerId, teamName, "PENDIENTE_FASE_3", player.getPosition()));
    }

    private String consultarPosicionJugadorEnAPI(String apiPlayerId) {
        String urlPlayer = "https://www.thesportsdb.com/api/v1/json/123/lookupplayer.php?id=" + apiPlayerId;

        try {
            String responseStr = restTemplate.getForObject(urlPlayer, String.class);
            JsonNode root = mapper.readTree(responseStr);
            JsonNode playersArray = root.path("players");

            if (playersArray.isArray() && !playersArray.isEmpty()) {
                JsonNode playerNode = playersArray.get(0);
                String position = playerNode.path("strPosition").asText();

                if (position != null && !position.isEmpty() && !position.equals("null")) {
                    return position; // Devuelve por ejemplo: "Forward", "Midfielder", "Defender", etc.
                }
            }
        } catch (Exception e) {
            System.err.println("      [ERR API PLAYER] No se pudo obtener la posición para el ID " + apiPlayerId + ": "
                    + e.getMessage());
        }

        return "UNKNOWN"; // Retorno de seguridad si falla la red o la API
    }

    private String normalizarPosicion(String apiPosition) {
        if (apiPosition == null || apiPosition.isEmpty() || apiPosition.equalsIgnoreCase("null")) {
            return "UNKNOWN";
        }

        String pos = apiPosition.toLowerCase();

        // Añadimos "attacker" a la lista de coincidencias
        if (pos.contains("forward") || pos.contains("winger") || pos.contains("striker") || pos.contains("wing")
                || pos.contains("attacker")) {
            return "ATTACKER";
        }

        if (pos.contains("midfield") || pos.contains("midfielder") || pos.contains("central midfield") || pos.contains("defensive lineman")) {
            return "MIDFIELDER";
        }

        if (pos.contains("back") || pos.contains("defender") || pos.contains("fullback") || pos.contains("sweeper")) {
            return "DEFENDER";
        }

        if (pos.contains("goalkeeper") || pos.contains("keeper")) {
            return "GOALKEEPER";
        }

        return "UNKNOWN";
    }

    public void repararPosicionesUnknown() {
        System.out.println(">>> Fase 3: Iniciando actualización de posiciones para jugadores UNKNOWN...");

        // 1. Limpiamos la lista de diagnóstico para ver qué jugadores reparamos en esta
        // tanda
        this.inicializarListaDebug();

        // 2. Buscamos en la base de datos TODOS los jugadores que estén en UNKNOWN
        // (Necesitas tener el método findAllByPosition o usar List<Player> y filtrar)
        List<Player> todosLosJugadores = playerRepo.findAll();

        for (Player player : todosLosJugadores) {
            // Solo procesamos los que no tengan posición válida o sean de simulación real
            if ("UNKNOWN".equals(player.getPosition()) || player.getPosition() == null) {

                // Saltamos los jugadores simulados (esos no existen en la API real)
                if (player.getApiPlayerId().startsWith("SIM_")) {
                    // Les asignamos una posición por descarte según su nombre genérico
                    if (player.getName().contains("Delantero"))
                        player.setPosition("ATTACKER");
                    if (player.getName().contains("Centrocampista"))
                        player.setPosition("MIDFIELDER");
                    playerRepo.save(player);
                    continue;
                }

                System.out.println("[PERFIL] Buscando posición en API para: " + player.getName() + " (ID: "
                        + player.getApiPlayerId() + ")");

                // 3. Vamos a la API a por la posición real
                String posicionAPI = consultarPosicionJugadorEnAPI(player.getApiPlayerId());
                String posicionNormalizada = normalizarPosicion(posicionAPI);

                player.setPosition(posicionNormalizada);
                playerRepo.save(player);

                // Guardamos en el informe de Postman
                this.debugList.add(new PlayerDebugInfo(player.getName(), player.getApiPlayerId(), player.getTeam(),
                        posicionAPI, posicionNormalizada));

                // ⏱️ PAUSA QUIRÚRGICA: Esperamos 3 segundos entre jugador y jugador.
                // Como este proceso va solo, la API jamás se va a colapsar.
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        System.out.println(">>> Fase 3: Proceso de reparación finalizado.");
    }

    private List<PlayerDebugInfo> debugList = new ArrayList<>();

    // === 2. MÉTODOS DE CONTROL PARA EL CONTROLADOR ===
    public void inicializarListaDebug() {
        this.debugList.clear(); // Vacía la lista antes de empezar la importación masiva
    }

    public List<PlayerDebugInfo> getDebugList() {
        return this.debugList; // Devuelve la lista al controlador al terminar
    }

    public static class PlayerDebugInfo {
        public String name;
        public String idApi;
        public String team;
        public String posicionOriginalApi;
        public String posicionMapeadaDb;

        public PlayerDebugInfo(String name, String idApi, String team, String posicionOriginalApi,
                String posicionMapeadaDb) {
            this.name = name;
            this.idApi = idApi;
            this.team = team;
            this.posicionOriginalApi = posicionOriginalApi;
            this.posicionMapeadaDb = posicionMapeadaDb;
        }
    }

    public void repararDiasDiezFaltantes() {
        System.out.println(">>> 🛠️ INICIANDO PROCESO DE REPARACIÓN DE LOS DÍAS 10 PARA TODAS LAS LIGAS...");

        // 1. Definimos las ligas que manejas en tu proyecto de forma dinámica
        String[] ligasId = { "4335", "4328", "4332", "4331", "4334" }; // ¡Cambia estos IDs por los reales de tus ligas!

        // 2. Definimos los meses que se quedaron con el "agujero" del día 10
        int[] mesesAfectados = { 1, 2, 3, 4 }; // Enero, Febrero, Marzo, Abril
        int anyo = 2026;

        for (int mes : mesesAfectados) {
            LocalDate diaDiez = LocalDate.of(anyo, mes, 10);
            LocalDate diaSiguiente = diaDiez.plusDays(1);

            System.out.println("\n=======================================================");
            System.out.println("📅 FECHA CRÍTICA: " + diaDiez);
            System.out.println("=======================================================");

            // 🔥 BUCLE DINÁMICO: Recorremos cada una de las ligas del array para este día
            for (String leagueId : ligasId) {
                System.out.println("\n[REPARADOR] ⚽ Procesando Liga ID: " + leagueId + " para el día " + diaDiez);

                // ==========================================
                // PASO A: REPARAR LOS MATCH_ID EN LA TABLA MATCHES
                // ==========================================
                try {
                    System.out.println("[REPARADOR] -> Lanzando importador dinámico para la liga " + leagueId);

                    // Llamamos a tu método pasándole las fechas y el ID de la liga correspondiente
                    importarPartidosPorRangoDinamico(diaDiez, diaSiguiente, leagueId);

                } catch (Exception e) {
                    System.err.println("[ERROR] No se pudieron actualizar los partidos de la liga " + leagueId
                            + " el día " + diaDiez + ": " + e.getMessage());
                }

                // Pausa de cortesía para que la API respire tras bajarse el calendario de esta
                // liga
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                }
            }

            // ==========================================
            // PASO B: INGESTAR EVENTOS DE LOS PARTIDOS REPARADOS
            // ==========================================
            // Una vez ejecutada la Fase 1 para todas las ligas de este día,
            // buscamos en la BD local todos los partidos que caigan en este rango de 24
            // horas.
            System.out.println("\n[REPARADOR] -> Buscando partidos reparados en la BD para el día " + diaDiez + "...");
            List<Match> partidosDelDia = matchRepo.findByDateBetween(diaDiez, diaSiguiente);

            int eventosProcesados = 0;
            for (Match partido : partidosDelDia) {
                String apiMatchId = partido.getApiMatchId();

                // Si el Paso A funcionó y guardó el ID, ahora entrará aquí
                if (apiMatchId != null && !apiMatchId.isEmpty()) {

                    // Comprobamos si ya tiene eventos para no duplicar ni gastar llamadas
                    boolean yaTieneEventos = matchEventRepo.existsByMatchId(apiMatchId);

                    if (!yaTieneEventos) {
                        System.out.println("[TIMELINE] Bajando eventos de: "
                                + partido.getHomeTeam() + " vs " + partido.getAwayTeam() + " (ID: " + apiMatchId + ")");

                        // 🔽 Pon aquí el método real de tu Fase 2 para importar los eventos
                        importTimelineEvents(apiMatchId);
                        eventosProcesados++;

                        // ⏱️ PAUSA ANTIBANEO: Crucial para evitar el error 429
                        try {
                            Thread.sleep(4000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
            System.out.println("[REPARADOR] ✅ Todo listo para el día " + diaDiez + ". Eventos nuevos inyectados: "
                    + eventosProcesados);
        }
        System.out.println("\n>>> 🛠️ PROCESO DE REPARACIÓN TERMINADO. ¡Todas las ligas y fechas reparadas!");
    }
}