package com.pfm.servicecommande.feignClient;

import com.pfm.servicecommande.exception.DownstreamServiceException;
import com.pfm.servicecommande.exception.ResourceNotFoundException;
import feign.FeignException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class CatalogueClientFallbackFactory implements FallbackFactory<CatalogueClient> {
    @Override
    public CatalogueClient create(Throwable cause) {
        return productId -> {
            if (cause instanceof FeignException.NotFound) {
                throw new ResourceNotFoundException("Produit introuvable ID: " + productId);
            }
            throw new DownstreamServiceException("Service catalogue indisponible", cause);
        };
    }
}
