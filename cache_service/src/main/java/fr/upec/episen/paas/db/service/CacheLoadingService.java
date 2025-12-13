package fr.upec.episen.paas.db.service;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import java.util.List;
import fr.upec.episen.paas.db.entity.Employe;
import fr.upec.episen.paas.db.repository.EmployeRepository;

@Service
public class CacheLoadingService {

    private final EmployeRepository repository;
    private final CacheService cacheService;

    public CacheLoadingService(EmployeRepository repository, CacheService cacheService) {
        this.repository = repository;
        this.cacheService = cacheService;
    }

    @Scheduled(fixedRate = 30000)
    public void refreshCache() {
        List<Employe> employees = repository.findAll();

        for (Employe e : employees) {
            cacheService.putEmploye(e); 
        }

        System.out.println("✅ Cache updated with " + employees.size() + " employees");
    }
}

