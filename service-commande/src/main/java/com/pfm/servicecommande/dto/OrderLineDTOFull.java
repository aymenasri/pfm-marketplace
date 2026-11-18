package com.pfm.servicecommande.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderLineDTOFull {
    private Long productId;
    private Integer quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal sousTotal;

    private String produitNom;
    private String produitDescription;
}