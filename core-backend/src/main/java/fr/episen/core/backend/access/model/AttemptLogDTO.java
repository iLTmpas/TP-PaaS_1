package fr.episen.core.backend.access.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event Kafka pour une tentative refusée
 * Topic: attempt-logs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttemptLogDTO {

    @JsonProperty("badge_id")
    private String badgeId;

    @JsonProperty("reason")
    private String reason;  // "UNKNOWN_BADGE" ou "NOT_VALIDE"

    @JsonProperty("timestamp")
    private Long timestamp;
}