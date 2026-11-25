package com.pfm.servicecommande.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.pfm.servicecommande.exception.DownstreamServiceException;
import com.pfm.servicecommande.exception.ResourceNotFoundException;
import com.pfm.servicecommande.feignClient.CatalogueClient;
import com.pfm.servicecommande.feignClient.ProductDTO;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CatalogueProductService {
    private static final Logger log = LoggerFactory.getLogger(CatalogueProductService.class);

    private final CatalogueClient catalogueClient;
    private final Cache<Long, ProductDTO> productCache;

    public CatalogueProductService(CatalogueClient catalogueClient,
                                   Cache<Long, ProductDTO> productCache) {
        this.catalogueClient = catalogueClient;
        this.productCache = productCache;
    }

    public ProductDTO getForOrder(Long productId) {
        try {
            ProductDTO product = catalogueClient.getProductById(productId);
            if (product == null) {
                throw new ResourceNotFoundException("Produit introuvable ID: " + productId);
            }
            productCache.put(productId, product);
            return product;
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Produit introuvable ID: " + productId);
        } catch (FeignException e) {
            throw new DownstreamServiceException("Service catalogue indisponible", e);
        }
    }

    public ProductDTO getForRead(Long productId) {
        try {
            return getForOrder(productId);
        } catch (DownstreamServiceException e) {
            ProductDTO cachedProduct = productCache.getIfPresent(productId);
            if (cachedProduct == null) {
                throw e;
            }
            log.warn("Catalogue indisponible; produit {} servi depuis le cache", productId);
            return cachedProduct;
        }
    }
}
