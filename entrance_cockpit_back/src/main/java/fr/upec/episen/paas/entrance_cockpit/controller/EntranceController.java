package fr.upec.episen.paas.entrance_cockpit.controller;

import fr.upec.episen.paas.entrance_cockpit.service.CockpitService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cockpit")
public class EntranceController {

    private final CockpitService cockpitService;

    public EntranceController(CockpitService cockpitService) {
        this.cockpitService = cockpitService;
    }

    /**
     * Endpoint pour autoriser un badge.
     * Le front ou un autre service POST le badgeId.
     */
    @PostMapping("/authorize/{badgeId}")
    public void authorizeBadge(@PathVariable Long badgeId) {
        // Appelle le service qui fait tout le travail
        cockpitService.authorizeBadgeAndNotifyFront(badgeId);
    }
}
