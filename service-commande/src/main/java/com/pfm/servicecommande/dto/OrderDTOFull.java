package com.pfm.servicecommande.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTOFull {
    private Long id;
    private String clientEmail;
    private LocalDateTime date;
    private String statut;
    private BigDecimal montantTotal;

    // The enriched list of lines
    private List<OrderLineDTOFull> lines;
}