package fr.upec.episen.paas.db.service;

import fr.upec.episen.paas.db.entity.Employe;
import fr.upec.episen.paas.db.repository.EmployeRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CacheService {

    private final EmployeRepository employeRepository;

    public CacheService(EmployeRepository employeRepository) {
        this.employeRepository = employeRepository;
    }

    @Cacheable("employes")
    public Employe getEmploye(Long id) {
        return employeRepository.findById(id).orElse(null);
    }

    public java.util.Collection<Employe> getAll() {
        return employeRepository.findAll();
    }

    public void updateCache(List<Employe> employes) {
        // update the cache
        for (Employe employe : employes) {
            getEmploye(employe.getId());
        }
    }
}
