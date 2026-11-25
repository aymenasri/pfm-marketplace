package com.pfm.servicecommande.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.pfm.servicecommande.feignClient.ProductDTO;

@FeignClient(
        name = "service-catalogue",
        configuration = FeignRetryConfiguration.class,
        fallbackFactory = CatalogueClientFallbackFactory.class
)
public interface CatalogueClient {

    @GetMapping("/api/products/{id}")
    ProductDTO getProductById(@PathVariable("id") Long id);
}