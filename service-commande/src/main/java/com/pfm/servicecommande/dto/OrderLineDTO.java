package com.pfm.servicecommande.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class OrderLineDTO {
    @NotNull(message = "Le productId est obligatoire")
    private Long productId;

    @NotNull(message = "La quantité est obligatoire")
    @Positive(message = "La quantité doit être positive")
    private Integer quantite;

    @NotNull(message = "Le prix unitaire est obligatoire")
    @Positive(message = "Le prix unitaire doit être positif")
    private BigDecimal prixUnitaire;

    // Getters et Setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer quantite) { this.quantite = quantite; }
    public BigDecimal getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(BigDecimal prixUnitaire) { this.prixUnitaire = prixUnitaire; }
}