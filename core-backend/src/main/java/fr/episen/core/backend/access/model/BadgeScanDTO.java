package fr.episen.core.backend.access.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Message MQTT reçu lors d'un scan de badge
 * Topic: iot/entrance/badge/scan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeScanDTO {

    @JsonProperty("badge_id")
    private String badgeId;

    @JsonProperty("reader_id")
    private String readerId;

    @JsonProperty("timestamp")
    private Long timestamp;
}