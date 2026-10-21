package com.pfm.servicestock.service;

import com.pfm.servicestock.dto.StockDTO;
import com.pfm.servicestock.entity.Stock;
import com.pfm.servicestock.exception.ResourceNotFoundException;
import com.pfm.servicestock.repository.StockRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StockService {

    private final StockRepository repository;

    public StockService(StockRepository repository) {
        this.repository = repository;
    }

    public List<StockDTO> getAll() {
        return repository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public StockDTO getById(Long id) {
        Stock stock = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock non trouvé avec l'id : " + id));
        return mapToDTO(stock);
    }

    public StockDTO create(StockDTO dto) {
        Stock stock = new Stock();
        stock.setProductId(dto.getProductId());
        stock.setQuantite(dto.getQuantite());
        stock.setSeuilAlerte(dto.getSeuilAlerte());
        return mapToDTO(repository.save(stock));
    }

    public StockDTO update(Long id, StockDTO dto) {
        Stock stock = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock non trouvé avec l'id : " + id));

        stock.setQuantite(dto.getQuantite());
        stock.setSeuilAlerte(dto.getSeuilAlerte());
        return mapToDTO(repository.save(stock));
    }

    private StockDTO mapToDTO(Stock entity) {
        StockDTO dto = new StockDTO();
        dto.setId(entity.getId());
        dto.setProductId(entity.getProductId());
        dto.setQuantite(entity.getQuantite());
        dto.setSeuilAlerte(entity.getSeuilAlerte());
        return dto;
    }
}