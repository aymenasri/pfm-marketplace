package com.pfm.servicecommande.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "service-stock",
        configuration = FeignRetryConfiguration.class,
        fallbackFactory = StockClientFallbackFactory.class
)
public interface StockClient {

    @GetMapping("/api/stocks/product/{productId}")
    StockDTO getStockByProductId(@PathVariable("productId") Long productId);
}