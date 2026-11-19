package com.pfm.servicecommande.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDTOFull {
    private Long id;
    private String clientEmail;
    private LocalDateTime date;
    private String statut;
    private BigDecimal montantTotal;
    private List<OrderLineDTOFull> lines;

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
    public List<OrderLineDTOFull> getLines() { return lines; }
    public void setLines(List<OrderLineDTOFull> lines) { this.lines = lines; }
}