package com.pfm.servicecommande.controller;

import com.pfm.servicecommande.dto.OrderDTO;
import com.pfm.servicecommande.service.OrderService;
import jakarta.validation.Valid;
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

    @PostMapping
    public ResponseEntity<OrderDTO> create(@Valid @RequestBody OrderDTO dto) {
        OrderDTO created = service.createOrder(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
}