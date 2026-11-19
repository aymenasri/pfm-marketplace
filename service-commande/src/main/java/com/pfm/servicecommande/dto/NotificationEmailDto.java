package com.pfm.servicecommande.dto;

public record NotificationEmailDto(
        String destinataire,
        String objet,
        String contenu
) {}
