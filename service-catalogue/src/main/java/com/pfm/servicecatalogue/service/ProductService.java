package com.pfm.servicecatalogue.service;

import com.pfm.servicecatalogue.dto.ProductDTO;
import com.pfm.servicecatalogue.entity.Product;
import com.pfm.servicecatalogue.exception.ResourceNotFoundException;
import com.pfm.servicecatalogue.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository repository;

    // Injection par constructeur
    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<ProductDTO> getAllProducts() {
        return repository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO getProductById(Long id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'id : " + id));
        return mapToDTO(product);
    }

    public ProductDTO createProduct(ProductDTO dto) {
        Product product = mapToEntity(dto);
        Product saved = repository.save(product);
        return mapToDTO(saved);
    }

    public void deleteProduct(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Impossible de supprimer : Produit non trouvé avec l'id : " + id);
        }
        repository.deleteById(id);
    }

    // --- Méthodes de mapping ---
    private ProductDTO mapToDTO(Product entity) {
        ProductDTO dto = new ProductDTO();
        dto.setId(entity.getId());
        dto.setNom(entity.getNom());
        dto.setDescription(entity.getDescription());
        dto.setPrix(entity.getPrix());
        dto.setCategorie(entity.getCategorie());
        return dto;
    }

    private Product mapToEntity(ProductDTO dto) {
        Product entity = new Product();
        entity.setNom(dto.getNom());
        entity.setDescription(dto.getDescription());
        entity.setPrix(dto.getPrix());
        entity.setCategorie(dto.getCategorie());
        return entity;
    }
}