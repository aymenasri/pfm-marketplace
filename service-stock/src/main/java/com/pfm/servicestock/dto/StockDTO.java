package com.pfm.servicestock.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class StockDTO {
    private Long id;

    @NotNull(message = "Le productId est obligatoire")
    private Long productId;

    @NotNull(message = "La quantité est obligatoire")
    @PositiveOrZero(message = "La quantité ne peut pas être négative")
    private Integer quantite;

    private Integer seuilAlerte;

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer quantite) { this.quantite = quantite; }
    public Integer getSeuilAlerte() { return seuilAlerte; }
    public void setSeuilAlerte(Integer seuilAlerte) { this.seuilAlerte = seuilAlerte; }
}