package fr.episen.core.backend.access.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service pour la logique d'autorisation
 * Décision simple: valide = "true" → GRANTED, sinon DENIED
 */
@Slf4j
@Service
public class AuthorizationService {

    /**
     * Vérifie si un employé est autorisé à entrer
     *
     * @param employe Map depuis Redis {id, nom, prenom, valide}
     * @return true si accès autorisé, false sinon
     */
    public boolean isGranted(Map<Object, Object> employe) {
        if (employe == null || employe.isEmpty()) {
            return false;
        }

        String valide = (String) employe.get("valide");
        return "true".equals(valide);
    }
}
