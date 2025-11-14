package fr.episen.core.backend.access.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service pour accéder au cache Redis
 * Format clé: badge:BADGE-123
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String KEY_PREFIX = "badge:";

    /**
     * Récupère un employé depuis Redis par son badge_id
     *
     * @param badgeId ID du badge (ex: "BADGE-123")
     * @return Map avec {id, nom, prenom, mail, valide} ou null si non trouvé
     */
    public Map<Object, Object> getEmployeByBadge(String badgeId) {
        String key = KEY_PREFIX + badgeId;

        try {
            Map<Object, Object> employe = redisTemplate.opsForHash().entries(key);

            if (employe.isEmpty()) {
                log.debug("Cache MISS: {}", badgeId);
                return null;
            }

            log.debug("Cache HIT: {}", badgeId);
            return employe;

        } catch (Exception e) {
            log.error("Erreur lecture Redis pour badge: {}", badgeId, e);
            return null;
        }
    }
}