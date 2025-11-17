package fr.episen.worker.replication.scheduler;

import fr.episen.worker.replication.service.PgToRedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReplicationScheduler {
    private static final Logger logger = LoggerFactory.getLogger(ReplicationScheduler.class);

    private final PgToRedisService pgToRedisService;

    public ReplicationScheduler(PgToRedisService pgToRedisService) {
        this.pgToRedisService = pgToRedisService;
    }

    @Scheduled(cron = "*/10 * * * * *")
    public void scheduledReplication() {
        logger.info("=== Début de la réplication planifiée ===");
        try {
            int count = pgToRedisService.replicateAllEmployes();
            logger.info("=== Réplication planifiée terminée avec succès: {} employés répliqués ===", count);
        } catch (Exception e) {
            logger.error("=== Erreur lors de la réplication planifiée ===", e);
        }
    }

}
