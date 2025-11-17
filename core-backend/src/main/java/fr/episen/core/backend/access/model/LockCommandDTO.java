package fr.episen.core.backend.access.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Commande MQTT envoyée à la serrure Topic: iot/entrance/lock/command */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockCommandDTO {

  @JsonProperty("action")
  private String action; // "UNLOCK" ou "LOCK"

  @JsonProperty("employe_id")
  private Long employeId;

  @JsonProperty("badge_id")
  private String badgeId;

  @JsonProperty("timestamp")
  private Long timestamp;
}
