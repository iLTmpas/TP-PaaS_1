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
    private final CacheLoadingService cacheLoadingService;

    public CacheController(CacheService cacheService, CacheLoadingService cacheLoadingService) {
        this.cacheService = cacheService;
        this.cacheLoadingService = cacheLoadingService;
    }

    @GetMapping
    public Collection<Employe> getAll() {
        return cacheService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employe> getById(@PathVariable Long id) {
        Employe e = cacheService.getEmploye(id);
        if (e == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(e);
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refreshCache() {
        cacheLoadingService.refreshCache();
        return ResponseEntity.ok("Cache refresh triggered");
    }
}
