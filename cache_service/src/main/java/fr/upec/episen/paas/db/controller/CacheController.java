package fr.upec.episen.paas.db.controller;

import fr.upec.episen.paas.db.entity.Employe;
import fr.upec.episen.paas.db.service.CacheLoadingService;
import fr.upec.episen.paas.db.service.CacheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/cache")
public class CacheController {

    private final CacheService cacheService;

    public CacheController(CacheService cacheService) {
        this.cacheService = cacheService;
    }
    @GetMapping("/{id}")
    public ResponseEntity<Employe> getById(@PathVariable Long id) {
        System.out.println("Requête reçue pour id=" + id);
        Employe e = cacheService.getEmploye(id);
        if (e == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(e);
    }
}

