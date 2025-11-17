package fr.episen.core.backend.access.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.episen.core.backend.access.model.LockCommandDTO;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Service pour publier des commandes MQTT vers la serrure */
@Slf4j
@Service
public class MqttService {

  @Value("${mqtt.broker-url}")
  private String brokerUrl;

  @Value("${mqtt.client-id}")
  private String clientId;

  @Value("${mqtt.topic-command}")
  private String topicCommand;

  private final ObjectMapper objectMapper;
  private MqttClient mqttClient;

  public MqttService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @PostConstruct
  public void init() {
    try {
      mqttClient = new MqttClient(brokerUrl, clientId + "-pub", new MemoryPersistence());

      MqttConnectOptions options = new MqttConnectOptions();
      options.setAutomaticReconnect(true);
      options.setCleanSession(true);

      mqttClient.connect(options);
      log.info("✅ MQTT Publisher connecté");

    } catch (MqttException e) {
      log.error("❌ Erreur connexion MQTT Publisher", e);
    }
  }

  @PreDestroy
  public void cleanup() {
    try {
      if (mqttClient != null && mqttClient.isConnected()) {
        mqttClient.disconnect();
        mqttClient.close();
      }
    } catch (MqttException e) {
      log.error("Erreur déconnexion MQTT", e);
    }
  }

  /** Publie une commande UNLOCK (ouvrir la serrure) */
  public void publishUnlock(Long employeId, String badgeId) {
    LockCommandDTO command =
        LockCommandDTO.builder()
            .action("UNLOCK")
            .employeId(employeId)
            .badgeId(badgeId)
            .timestamp(System.currentTimeMillis())
            .build();

    publish(command);
  }

  /** Publie une commande LOCK (garder fermé) */
  public void publishLock(Long employeId, String badgeId) {
    LockCommandDTO command =
        LockCommandDTO.builder()
            .action("LOCK")
            .employeId(employeId)
            .badgeId(badgeId)
            .timestamp(System.currentTimeMillis())
            .build();

    publish(command);
  }

  /** Publie la commande MQTT */
  private void publish(LockCommandDTO command) {
    try {
      String payload = objectMapper.writeValueAsString(command);
      MqttMessage message = new MqttMessage(payload.getBytes());
      message.setQos(1);

      mqttClient.publish(topicCommand, message);
      log.debug("📤 MQTT publié: {} → {}", topicCommand, command.getAction());

    } catch (Exception e) {
      log.error("❌ Erreur publication MQTT", e);
    }
  }
}
