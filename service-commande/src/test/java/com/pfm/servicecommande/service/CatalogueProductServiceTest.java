package com.pfm.servicecommande.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.pfm.servicecommande.exception.DownstreamServiceException;
import com.pfm.servicecommande.feignClient.CatalogueClient;
import com.pfm.servicecommande.feignClient.ProductDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CatalogueProductServiceTest {
    @Test
    void readUsesCachedProductWhenCatalogueIsUnavailable() {
        ProductDTO cachedProduct = new ProductDTO();
        cachedProduct.setId(7L);
        Cache<Long, ProductDTO> cache = Caffeine.newBuilder().build();
        cache.put(7L, cachedProduct);
        CatalogueClient unavailableCatalogue = productId -> {
            throw new DownstreamServiceException("Service catalogue indisponible");
        };
        CatalogueProductService service = new CatalogueProductService(unavailableCatalogue, cache);

        assertSame(cachedProduct, service.getForRead(7L));
    }

    @Test
    void orderLookupFailsStrictlyWhenCatalogueIsUnavailableEvenWithCachedProduct() {
        ProductDTO cachedProduct = new ProductDTO();
        cachedProduct.setId(7L);
        Cache<Long, ProductDTO> cache = Caffeine.newBuilder().build();
        cache.put(7L, cachedProduct);
        CatalogueClient unavailableCatalogue = productId -> {
            throw new DownstreamServiceException("Service catalogue indisponible");
        };
        CatalogueProductService service = new CatalogueProductService(unavailableCatalogue, cache);

        assertThrows(DownstreamServiceException.class, () -> service.getForOrder(7L));
    }
}
