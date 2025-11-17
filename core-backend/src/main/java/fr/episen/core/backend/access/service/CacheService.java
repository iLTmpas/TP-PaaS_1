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

    // Template Redis pour effectuer les opérations sur le cache
    // Injecté automatiquement par Spring via le constructeur (@RequiredArgsConstructor)
    private final RedisTemplate<String, String> redisTemplate;

    // Préfixe utilisé pour toutes les clés Redis liées aux badges
    private static final String KEY_PREFIX = "badge:";

    /**
     * Récupère un employé depuis Redis par son badge_id
     *
     * @param badgeId ID du badge (ex: "BADGE-123")
     * @return Map avec {id, nom, prenom, mail, valide} ou null si non trouvé
     */
    public Map<Object, Object> getEmployeByBadge(String badgeId) {
        // Construction de la clé Redis complète (ex: "badge:BADGE-123")
        String key = KEY_PREFIX + badgeId;

        try {
            // Récupération de toutes les entrées du hash Redis pour cette clé
            // Un hash Redis stocke plusieurs paires clé-valeur sous une seule clé principale
            Map<Object, Object> employe = redisTemplate.opsForHash().entries(key);

            // Vérification si le hash est vide (badge non trouvé dans le cache)
            if (employe.isEmpty()) {
                // Log en mode debug pour le cache miss (donnée non présente)
                log.debug("Cache MISS: {}", badgeId);
                return null;
            }

            // Log en mode debug pour le cache hit (donnée trouvée dans le cache)
            log.debug("Cache HIT: {}", badgeId);
            // Retour des données de l'employé sous forme de Map
            return employe;

        } catch (Exception e) {
            // Gestion des erreurs de connexion ou de lecture Redis
            // Log avec le détail de l'exception pour faciliter le débogage
            log.error("Erreur lecture Redis pour badge: {}", badgeId, e);
            // Retour null en cas d'erreur pour permettre un fallback éventuel
            return null;
        }
    }
}