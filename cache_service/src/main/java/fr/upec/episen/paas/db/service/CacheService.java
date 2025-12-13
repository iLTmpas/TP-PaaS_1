package fr.upec.episen.paas.db.service;

import fr.upec.episen.paas.db.entity.Employe;
import fr.upec.episen.paas.db.repository.EmployeRepository;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CacheService {

    @Cacheable(value = "employes", key = "#id")
    public Employe getEmploye(Long id) {
        return null;  // Redis renverra la valeur si elle existe
    }

    @CachePut(value = "employes", key = "#employe.id")
    public Employe putEmploye(Employe employe){
        return employe; // --> écrit dans Redis
    }
}
