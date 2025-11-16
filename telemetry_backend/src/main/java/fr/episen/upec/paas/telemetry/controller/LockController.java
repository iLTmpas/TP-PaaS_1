package fr.episen.upec.paas.telemetry.controller;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.*;
import fr.episen.upec.paas.telemetry.service.TelemetryService;

@RestController
@RequestMapping("/lock")
public class LockController {

    private final TelemetryService telemetryService;

    public LockController(TelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    @PostMapping("/control")
    public String controlLock(@RequestParam String action) throws MqttException {
        telemetryService.sendLockCommand(action);
        return "Command sent: " + action;
    }
}
