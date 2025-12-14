package fr.upec.episen.paas.core.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller dédié au watchdog pour la surveillance du service
 */
@RestController
@RequestMapping("/api/watchdog")
public class WatchdogController {

    /**
     * Endpoint de health check pour le watchdog
     * 
     * @return Un objet contenant le statut et le timestamp
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        response.put("service", "core_backend");
        return response;
    }

    /**
     * Endpoint simple pour vérifier la disponibilité (réponse minimaliste)
     * 
     * @return "OK" si le service est disponible
     */
    @GetMapping("/ping")
    public String ping() {
        return "OK";
    }
}
