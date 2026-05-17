package com.proyecto.estadisticas_deportivas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class TimeLineResponseDTO {
    
    // El JSON de la API empieza con una lista llamada "timeline"
    private List<TimelineEvent> timeline;

    @Data
    public static class TimelineEvent {
        @JsonProperty("idEvent")
        private String matchId;

        @JsonProperty("strTimeline")
        private String eventType; // Aquí vendrá "Goal", "Assist", etc.

        @JsonProperty("strPlayer")
        private String playerName;

        @JsonProperty("idPlayer")
        private String apiPlayerId;

        @JsonProperty("intMinute")
        private int minute;

        @JsonProperty("strHomeAway")
        private String homeAway; // "Home" o "Away"
    }
}