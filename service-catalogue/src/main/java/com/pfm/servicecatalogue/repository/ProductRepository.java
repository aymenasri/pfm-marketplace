package com.pfm.servicecatalogue.repository;

import com.pfm.servicecatalogue.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Spring Data JPA génère automatiquement le CRUD de base
}