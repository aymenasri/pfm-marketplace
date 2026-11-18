package com.pfm.servicecommande.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "service-stock")
public interface StockClient {

    // Check your StockController in service-stock to ensure this is the exact URL
    @GetMapping("/api/stocks/{productId}")
    StockDTO getStockByProductId(@PathVariable("productId") Long productId);
}