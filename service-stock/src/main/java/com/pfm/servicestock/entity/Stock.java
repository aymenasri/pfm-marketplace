package com.pfm.servicestock.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "stocks")
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;
    private Integer quantite;
    private Integer seuilAlerte;

    public Stock() {}

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