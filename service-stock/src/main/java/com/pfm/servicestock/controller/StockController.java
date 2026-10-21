package com.pfm.servicestock.controller;

import com.pfm.servicestock.dto.StockDTO;
import com.pfm.servicestock.service.StockService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService service;

    public StockController(StockService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<StockDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<StockDTO> create(@Valid @RequestBody StockDTO dto) {
        return new ResponseEntity<>(service.create(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StockDTO> update(@PathVariable Long id, @Valid @RequestBody StockDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }
}