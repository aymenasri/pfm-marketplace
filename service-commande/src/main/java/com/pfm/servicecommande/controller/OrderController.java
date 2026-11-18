package com.pfm.servicecommande.controller;

import com.pfm.servicecommande.dto.OrderDTO;
import com.pfm.servicecommande.dto.OrderDTOFull; // Import the new DTO
import com.pfm.servicecommande.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAll() {
        return ResponseEntity.ok(service.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getOrderById(id));
    }

    // NEW ENDPOINT: Enriched Consultation
    @GetMapping("/{id}/detail")
    public ResponseEntity<OrderDTOFull> getOrderRichById(@PathVariable Long id) {
        OrderDTOFull dtoFull = service.getOrderFullById(id);
        return ResponseEntity.ok(dtoFull);
    }

    @PostMapping
    public ResponseEntity<OrderDTO> create(
            @RequestBody OrderDTO dto,
            @RequestHeader(value = "idempotencyKey") String idempotencyKey) {

        return new ResponseEntity<>(service.createOrder(dto, idempotencyKey), HttpStatus.CREATED);
    }
}