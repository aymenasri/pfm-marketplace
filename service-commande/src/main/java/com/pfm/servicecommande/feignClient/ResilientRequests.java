package com.pfm.servicecommande.feignClient;

import com.pfm.servicecommande.exception.ServiceExterneIndisponibleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;

@Slf4j
@Service
public class ResilientRequests {

    private final CatalogueClient catalogueClient;
    private final StockClient stockClient;
    private final CacheManager cacheManager;

    public ResilientRequests(CatalogueClient catalogueClient, StockClient stockClient, CacheManager cacheManager) {
        this.catalogueClient = catalogueClient;
        this.stockClient = stockClient;
        this.cacheManager = cacheManager;
    }

    // ========== LECTURE : Repli tolérant (Produits) ==========
    @Retry(name = "catalogue-service", fallbackMethod = "produitDeRepli")
    @CircuitBreaker(name = "catalogue-service")
    @Bulkhead(name = "catalogue-service")
    public ProductDTO getProduitByIdFb(Long id) {
        ProductDTO produit = catalogueClient.getProductById(id); // Fixed method name
        mettreEnCache("produits", id, produit);
        log.info("Produit {} put in cache", id);
        return produit;
    }

    private ProductDTO produitDeRepli(Long id, RuntimeException ex) {
        if (!estIndisponible(ex)) {
            throw ex;
        }
        ProductDTO enCache = lireCache("produits", id, ProductDTO.class);
        log.info("Produit {} loaded from cache OR null", id);
        if (enCache != null) return enCache;

        // If not in cache, return null. (Since ProductDTO is likely a record, we can't instantiate an empty one easily)
        return null;
    }

    // ========== LECTURE : Repli tolérant (Stocks) ==========
    @Retry(name = "stock-service", fallbackMethod = "stockDeRepli")
    @CircuitBreaker(name = "stock-service")
    @Bulkhead(name = "stock-service")
    public StockDTO getStockByIdFb(Long id) {
        StockDTO stock = stockClient.getStockByProductId(id); // Fixed method name
        mettreEnCache("stocks", id, stock);
        log.info("Stock {} put in cache", id);
        return stock;
    }

    private StockDTO stockDeRepli(Long id, RuntimeException ex) {
        if (!estIndisponible(ex)) {
            throw ex;
        }
        StockDTO enCache = lireCache("stocks", id, StockDTO.class);
        log.info("Stock {} loaded from cache OR null", id);
        return enCache; // Returns null if empty
    }

    // ========== CRÉATION : Repli strict (Produits) ==========
    @Retry(name = "catalogue-service", fallbackMethod = "produitIndisponible")
    @CircuitBreaker(name = "catalogue-service")
    @Bulkhead(name = "catalogue-service")
    public ProductDTO getProduitByIdStrict(Long id) {
        ProductDTO produit = catalogueClient.getProductById(id); // Fixed method name
        mettreEnCache("produits", id, produit);
        log.info("Produit {} put in cache", id);
        return produit;
    }

    private ProductDTO produitIndisponible(Long id, RuntimeException ex) {
        if (!estIndisponible(ex)) {
            throw ex;
        }
        throw new ServiceExterneIndisponibleException("Catalogue-Service est momentanément indisponible. Réessayez dans quelques instants.");
    }

    // ========== CRÉATION : Repli strict (Stocks) ==========
    @Retry(name = "stock-service", fallbackMethod = "stockIndisponible")
    @CircuitBreaker(name = "stock-service")
    @Bulkhead(name = "stock-service")
    public StockDTO getStockByIdStrict(Long id) {
        StockDTO stock = stockClient.getStockByProductId(id); // Fixed method name
        mettreEnCache("stocks", id, stock);
        log.info("Stock {} put in cache", id);
        return stock;
    }

    private StockDTO stockIndisponible(Long id, RuntimeException ex) {
        if (!estIndisponible(ex)) {
            throw ex;
        }
        throw new ServiceExterneIndisponibleException("Stock-Service est momentanément indisponible. Réessayez dans quelques instants.");
    }

    // ========== UTILS ==========
    private boolean estIndisponible(RuntimeException ex) {
        return ex instanceof feign.RetryableException ||
                (ex instanceof feign.FeignException && ((feign.FeignException) ex).status() == 503);
    }

    private void mettreEnCache(String nomCache, Object cle, Object valeur) {
        Cache cache = cacheManager.getCache(nomCache);
        if (cache != null) {
            cache.put(cle, valeur);
        }
    }

    private <T> T lireCache(String nomCache, Object cle, Class<T> type) {
        Cache cache = cacheManager.getCache(nomCache);
        return cache != null ? cache.get(cle, type) : null;
    }
}