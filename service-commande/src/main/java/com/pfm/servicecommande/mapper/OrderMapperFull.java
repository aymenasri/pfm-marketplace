package com.pfm.servicecommande.mapper;

import com.pfm.servicecommande.dto.OrderDTOFull;
import com.pfm.servicecommande.dto.OrderLineDTOFull;
import com.pfm.servicecommande.entity.Order;
import com.pfm.servicecommande.entity.OrderLine;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class OrderMapperFull {

    public OrderDTOFull toDto(Order order) {
        OrderDTOFull dto = new OrderDTOFull();
        dto.setId(order.getId());
        dto.setClientEmail(order.getClientEmail());
        dto.setDate(order.getDate());
        dto.setStatut(order.getStatut());
        dto.setMontantTotal(order.getMontantTotal());

        dto.setLines(order.getLines().stream().map(this::toLineDto).collect(Collectors.toList()));
        return dto;
    }

    private OrderLineDTOFull toLineDto(OrderLine line) {
        OrderLineDTOFull dto = new OrderLineDTOFull();
        dto.setProductId(line.getProductId());
        dto.setQuantite(line.getQuantite());
        dto.setPrixUnitaire(line.getPrixUnitaire());
        return dto;
    }
}