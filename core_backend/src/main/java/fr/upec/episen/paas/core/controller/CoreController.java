package fr.upec.episen.paas.core.controller;

import fr.upec.episen.paas.core.service.CoreOperationalService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/core")
public class CoreController {

    private final CoreOperationalService coreOperationalService;

    public CoreController(CoreOperationalService coreOperationalService) {
        this.coreOperationalService = coreOperationalService;
    }

    /**
     * Endpoint pour autoriser l'entrée d'un employé via son badge
     * @param badgeId L'ID du badge
     * @return "ACCESS GRANTED" ou "ACCESS DENIED"
     */
    @PostMapping("/authorize/{badgeId}")
    public String authorize(@PathVariable Long badgeId) {
        return coreOperationalService.processEntrance(badgeId);
    }
}
