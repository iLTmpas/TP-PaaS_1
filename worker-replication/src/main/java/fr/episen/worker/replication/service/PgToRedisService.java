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
    private static final String REDIS_ALL_EMPLOYES_KEY = "employes:all";
    private static final String REDIS_KEY_PREFIX = "employe:";

    private final EmployeRepository employeRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public PgToRedisService(EmployeRepository employeRepository, RedisTemplate<String, String> redisTemplate) {
        this.employeRepository = employeRepository;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Réplique tous les employés de PostgreSQL vers Redis
     *
     * @return nombre d'employés répliqués
     */
    public int replicateAllEmployes() {
        logger.info("Début de la réplication de tous les employés vers Redis");

        List<EmployeEntity> employes = employeRepository.findAll();
        int count = 0;

        for (EmployeEntity employe : employes) {
            try {
                replicateEmploye(employe);
                count++;
            } catch (Exception e) {
                logger.error("Erreur lors de la réplication de l'employé ID: {}", employe.getId(), e);
            }
        }

        logger.info("Réplication terminée: {} employés répliqués sur {}", count, employes.size());
        return count;
    }

    /**
     * Réplique un employé spécifique vers Redis
     *
     * @param employe l'entité à répliquer
     */
    public void replicateEmploye(EmployeEntity employe) {
        String key = REDIS_KEY_PREFIX + employe.getId();

        // Stocke toutes les informations de l'employé dans un Hash Redis
        redisTemplate.opsForHash().put(key, "id", employe.getId().toString());
        redisTemplate.opsForHash().put(key, "valide", employe.getValide().toString());

        // Ajoute l'ID à un set pour pouvoir lister tous les employés
        redisTemplate.opsForSet().add(REDIS_ALL_EMPLOYES_KEY, employe.getId().toString());

        logger.debug("Employé répliqué: {} {} (ID: {})", employe.getPrenom(), employe.getNom(), employe.getId());
    }


}
