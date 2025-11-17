package fr.episen.worker.replication.service;

import fr.episen.worker.replication.entity.EmployeEntity;
import fr.episen.worker.replication.repository.EmployeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PgToRedisService {
    private static final Logger logger = LoggerFactory.getLogger(PgToRedisService.class);

    /**
     * Préfixe des clés Redis pour identifier les badges.
     * Format final : "badge:{UUID}" (ex: badge:550e8400-e29b-41d4-a716-446655440000)
     * Ce préfixe permet de :
     * - Namespace les données (éviter les collisions avec d'autres clés)
     * - Faciliter les requêtes globales (KEYS badge:*, SCAN badge:*)
     * - Organiser logiquement les données dans Redis
     */
    private static final String REDIS_KEY_PREFIX = "badge:";

    private final EmployeRepository employeRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public PgToRedisService(EmployeRepository employeRepository, RedisTemplate<String, String> redisTemplate) {
        this.employeRepository = employeRepository;
        this.redisTemplate = redisTemplate;
    }

    public int replicateAllEmployes() {
        logger.info("Début réplication vers Redis (format badge:*)");

        List<EmployeEntity> employes = employeRepository.findAll();
        int count = 0;

        for (EmployeEntity employe : employes) {
            try {
                replicateEmploye(employe);
                count++;
            } catch (Exception e) {
                logger.error("Erreur réplication employé ID: {}", employe.getIdEmploye(), e);
            }
        }

        logger.info("Réplication terminée: {} employés", count);
        return count;
    }

    public void replicateEmploye(EmployeEntity employe) {
        if (employe.getBadgeId() == null) {
            logger.warn("Employé {} {} n'a pas de badge_id, skip", employe.getPrenom(), employe.getNom());
            return;
        }

        String key = REDIS_KEY_PREFIX + employe.getBadgeId();
        // Commande Redis : HSET badge:xxx id "123"
        // - opsForHash() : opérations sur structures Hash
        // - put(key, field, value) : définit la valeur d'un champ
        // - toString() : conversion Long → String (sérialiseur configuré)
        redisTemplate.opsForHash().put(key, "id", employe.getIdEmploye().toString());
        redisTemplate.opsForHash().put(key, "valide", employe.getValide().toString());

        logger.debug("Répliqué: badge {} → employé {} {} (valide: {})", employe.getBadgeId(), employe.getPrenom(), employe.getNom(), employe.getValide());
    }

}
