package com.pfm.servicecommande.controller;

import com.pfm.servicecommande.dto.OrderDTO;
import com.pfm.servicecommande.dto.OrderDTOFull;
import com.pfm.servicecommande.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

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

    @GetMapping("/{id}/detail")
    public ResponseEntity<OrderDTOFull> getOrderRichById(@PathVariable Long id) {
        OrderDTOFull dtoFull = service.getOrderFullById(id);
        return ResponseEntity.ok(dtoFull);
    }

    @PostMapping
    public ResponseEntity<OrderDTO> create(
            @Valid @RequestBody OrderDTO dto,
            @RequestHeader(value = "Idempotency-Key", required = false) String standardKey,
            @RequestHeader(value = "idempotencyKey", required = false) String legacyKey) {
        String key = standardKey != null ? standardKey : legacyKey;
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Clé d'idempotence manquante");
        }
        boolean replay = service.isIdempotencyKeyProcessed(key);
        OrderDTO created = service.createOrder(dto, key);
        return new ResponseEntity<>(created, replay ? HttpStatus.OK : HttpStatus.CREATED);
    }
}