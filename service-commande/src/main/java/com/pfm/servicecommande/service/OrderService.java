package com.pfm.servicecommande.service;

import com.pfm.servicecommande.dto.OrderDTO;
import com.pfm.servicecommande.dto.OrderLineDTO;
import com.pfm.servicecommande.entity.Order;
import com.pfm.servicecommande.entity.OrderLine;
import com.pfm.servicecommande.exception.ResourceNotFoundException;
import com.pfm.servicecommande.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public List<OrderDTO> getAllOrders() {
        return repository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public OrderDTO getOrderById(Long id) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'id : " + id));
        return mapToDTO(order);
    }

    public OrderDTO createOrder(OrderDTO dto) {
        Order order = new Order();
        order.setClientEmail(dto.getClientEmail());
        order.setDate(LocalDateTime.now());
        order.setStatut("EN_ATTENTE");

        BigDecimal total = BigDecimal.ZERO;
        for (OrderLineDTO lineDto : dto.getLines()) {
            OrderLine line = new OrderLine();
            line.setProductId(lineDto.getProductId());
            line.setQuantite(lineDto.getQuantite());
            line.setPrixUnitaire(lineDto.getPrixUnitaire());
            line.setOrder(order); // Lien avec la commande
            order.getLines().add(line);

            // Calcul du total : quantité * prix
            total = total.add(lineDto.getPrixUnitaire().multiply(new BigDecimal(lineDto.getQuantite())));
        }
        order.setMontantTotal(total);

        Order saved = repository.save(order);
        return mapToDTO(saved);
    }

    // --- Mapper ---
    private OrderDTO mapToDTO(Order entity) {
        OrderDTO dto = new OrderDTO();
        dto.setId(entity.getId());
        dto.setClientEmail(entity.getClientEmail());
        dto.setDate(entity.getDate());
        dto.setStatut(entity.getStatut());
        dto.setMontantTotal(entity.getMontantTotal());

        List<OrderLineDTO> lines = entity.getLines().stream().map(l -> {
            OrderLineDTO ldto = new OrderLineDTO();
            ldto.setProductId(l.getProductId());
            ldto.setQuantite(l.getQuantite());
            ldto.setPrixUnitaire(l.getPrixUnitaire());
            return ldto;
        }).collect(Collectors.toList());

        dto.setLines(lines);
        return dto;
    }
}