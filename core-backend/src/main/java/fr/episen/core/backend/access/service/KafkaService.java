package fr.episen.core.backend.access.service;

import fr.episen.core.backend.access.model.AttemptLogDTO;
import fr.episen.core.backend.access.model.EntranceLogDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/** Service pour publier des événements dans Kafka */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaService {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Value("${kafka.topic-entrance}")
  private String topicEntrance;

  @Value("${kafka.topic-attempt}")
  private String topicAttempt;

  /** Publie un événement d'entrée autorisée (GRANTED) */
  public void publishEntranceLog(Long employeId, String badgeId, String nom, String prenom) {
    EntranceLogDTO entranceLogDTO =
        EntranceLogDTO.builder()
            .employeId(employeId)
            .badgeId(badgeId)
            .nom(nom)
            .prenom(prenom)
            .timestamp(System.currentTimeMillis())
            .build();

    try {
      kafkaTemplate.send(topicEntrance, badgeId, entranceLogDTO);
      log.debug("📤 Kafka entrance-log: {} {}", prenom, nom);

    } catch (Exception e) {
      log.error("❌ Erreur publication Kafka entrance-log", e);
    }
  }

  /** Publie un événement de tentative refusée (DENIED) */
  public void publishAttemptLog(String badgeId, String reason) {
    AttemptLogDTO attemptLogDTO =
        AttemptLogDTO.builder()
            .badgeId(badgeId)
            .reason(reason)
            .timestamp(System.currentTimeMillis())
            .build();
    try {
      kafkaTemplate.send(topicAttempt, badgeId, attemptLogDTO);
      log.debug("📤 Kafka attempt-log: {} ({})", badgeId, reason);

    } catch (Exception e) {
      log.error("❌ Erreur publication Kafka attempt-log", e);
    }
  }
}
