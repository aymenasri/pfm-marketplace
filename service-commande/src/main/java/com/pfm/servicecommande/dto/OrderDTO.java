package com.pfm.servicecommande.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDTO {
    private Long id;

    @NotBlank(message = "L'email du client est obligatoire")
    @Email(message = "Format d'email invalide")
    private String clientEmail;

    private LocalDateTime date;
    private String statut;
    private BigDecimal montantTotal;

    @NotEmpty(message = "La commande doit contenir au moins une ligne")
    @Valid // Permet de valider les lignes à l'intérieur
    private List<OrderLineDTO> lines;

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getClientEmail() { return clientEmail; }
    public void setClientEmail(String clientEmail) { this.clientEmail = clientEmail; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public BigDecimal getMontantTotal() { return montantTotal; }
    public void setMontantTotal(BigDecimal montantTotal) { this.montantTotal = montantTotal; }
    public List<OrderLineDTO> getLines() { return lines; }
    public void setLines(List<OrderLineDTO> lines) { this.lines = lines; }
}