package com.pfm.servicecommande.feignClient;

import com.pfm.servicecommande.exception.DownstreamServiceException;
import com.pfm.servicecommande.exception.ResourceNotFoundException;
import feign.FeignException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class StockClientFallbackFactory implements FallbackFactory<StockClient> {
    @Override
    public StockClient create(Throwable cause) {
        return productId -> {
            if (cause instanceof FeignException.NotFound) {
                throw new ResourceNotFoundException("Stock introuvable pour le produit ID: " + productId);
            }
            throw new DownstreamServiceException("Service stock indisponible", cause);
        };
    }
}
