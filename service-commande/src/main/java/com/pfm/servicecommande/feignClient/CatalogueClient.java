package com.pfm.servicecommande.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "service-catalogue")
public interface CatalogueClient {

    // Change "/api/products/{id}" to whatever the actual URL is in your Catalogue Controller
    @GetMapping("/api/products/{id}")
    ProductDTO getProductById(@PathVariable("id") Long id);
}