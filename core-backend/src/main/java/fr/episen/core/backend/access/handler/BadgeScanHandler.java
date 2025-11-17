package fr.episen.core.backend.access.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.episen.core.backend.access.model.BadgeScanDTO;
import fr.episen.core.backend.access.service.AuthorizationService;
import fr.episen.core.backend.access.service.CacheService;
import fr.episen.core.backend.access.service.KafkaService;
import fr.episen.core.backend.access.service.MqttService;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * Handler principal pour les messages MQTT de scan de badge
 *
 * <p>Flow : 1. Reçoit message MQTT 2. vérifie Redis 3. Décision autorisation 4. Publish commande
 * MQTT (serrure) 5. Publish événement Kafka (logs)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BadgeScanHandler {

    private final CacheService cacheService;
    private final AuthorizationService authorizationService;
    private final MqttService mqttService;
    private final KafkaService kafkaService;
    private final ObjectMapper objectMapper;

    public void handleMessage(Message<?> message) {
        long startTime = System.currentTimeMillis();

        try {
            // 1. Parser le payload JSON
            BadgeScanDTO scan = objectMapper.readValue(message.getPayload().toString(), BadgeScanDTO.class);

            log.info("🔖 Badge scanné: {} ", scan.getBadgeId());

            // 2. Lookup Redis
            Map<Object, Object> employe = cacheService.getEmployeByBadge(scan.getBadgeId());

            log.info("Récupération depuis Redis: {} ", employe);

            if (employe == null || employe.isEmpty()) {
                // Badge inconnu
                handleUnknownBadge(scan);
                logLatency(startTime);
                return;
            }

            // 3. Décision d'autorisation
            boolean isGranted = authorizationService.isGranted(employe);

            Long employeId = Long.parseLong((String) employe.get("id"));

            if (isGranted) {
                // ✅ GRANTED
                log.info("✅ GRANTED -ID: {})", employeId);

                // 4. Publier commande serrure (UNLOCK)
                mqttService.publishUnlock(employeId, scan.getBadgeId());

                // 5. Publier événement Kafka (entrance-logs)
                kafkaService.publishEntranceLog(employeId, scan.getBadgeId());

            } else {
                // ❌ DENIED
                log.warn("❌ DENIED - ID: {} - NOT_VALIDE", employeId);

                // 4. Publier commande serrure (LOCK)
                mqttService.publishLock(employeId, scan.getBadgeId());

                // 5. Publier événement Kafka (attempt-logs)
                kafkaService.publishAttemptLog(scan.getBadgeId(), "NOT_VALIDE");
            }

            logLatency(startTime);

        } catch (Exception e) {
            log.error("❌ Erreur traitement badge scan", e);
        }
    }

    /**
     * Gère le cas d'un badge inconnu
     */
    private void handleUnknownBadge(BadgeScanDTO scan) {
        log.warn("⚠️ Badge inconnu: {}", scan.getBadgeId());

        // Publier commande serrure (LOCK)
        mqttService.publishLock(null, scan.getBadgeId());

        // Publier événement Kafka (attempt-logs)
        kafkaService.publishAttemptLog(scan.getBadgeId(), "UNKNOWN_BADGE");
    }

    /**
     * Log la latency totale
     */
    private void logLatency(long startTime) {
        long latency = System.currentTimeMillis() - startTime;
        log.debug("⏱️ Latency totale: {}ms", latency);

        if (latency > 50) {
            log.warn("⚠️ Latency > 50ms : {}ms", latency);
        }
    }
}
