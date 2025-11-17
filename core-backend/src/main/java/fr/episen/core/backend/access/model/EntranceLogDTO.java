package fr.episen.core.backend.access.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event Kafka pour une entrée autorisée
 * Topic: entrance-logs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntranceLogDTO {

    @JsonProperty("employe_id")
    private Long employeId;

    @JsonProperty("badge_id")
    private String badgeId;

    @JsonProperty("timestamp")
    private Long timestamp;
}