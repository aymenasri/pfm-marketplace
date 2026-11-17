package com.pfm.servicenotification.controller;

import com.pfm.servicenotification.dto.NotificationEmailDto;
import com.pfm.servicenotification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/email")
    public ResponseEntity<Void> envoyer(@Valid @RequestBody NotificationEmailDto dto) {
        notificationService.envoyerEmail(dto);
        return ResponseEntity.noContent().build();
    }
}