package com.pfm.servicecommande.dto;

import java.math.BigDecimal;

public class OrderLineDTOFull {
    private Long productId;
    private Integer quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal sousTotal;
    private String produitNom;
    private String produitDescription;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer quantite) { this.quantite = quantite; }
    public BigDecimal getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(BigDecimal prixUnitaire) { this.prixUnitaire = prixUnitaire; }
    public BigDecimal getSousTotal() { return sousTotal; }
    public void setSousTotal(BigDecimal sousTotal) { this.sousTotal = sousTotal; }
    public String getProduitNom() { return produitNom; }
    public void setProduitNom(String produitNom) { this.produitNom = produitNom; }
    public String getProduitDescription() { return produitDescription; }
    public void setProduitDescription(String produitDescription) { this.produitDescription = produitDescription; }
}