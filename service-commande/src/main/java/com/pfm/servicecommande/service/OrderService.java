package com.pfm.servicecommande.service;

import com.pfm.servicecommande.dto.OrderDTO;
import com.pfm.servicecommande.dto.OrderDTOFull;
import com.pfm.servicecommande.dto.OrderLineDTO;
import com.pfm.servicecommande.dto.OrderLineDTOFull;
import com.pfm.servicecommande.entity.Order;
import com.pfm.servicecommande.entity.OrderLine;
import com.pfm.servicecommande.exception.ResourceNotFoundException;
import com.pfm.servicecommande.feignClient.CatalogueClient;
import com.pfm.servicecommande.feignClient.StockClient;
import com.pfm.servicecommande.feignClient.ProductDTO;
import com.pfm.servicecommande.feignClient.StockDTO;
import com.pfm.servicecommande.mapper.OrderMapperFull;
import com.pfm.servicecommande.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final CatalogueClient catalogueClient;
    private final StockClient stockClient;
    private final OrderMapperFull orderMapperFull; // Injected Mapper

    // Updated Constructor
    public OrderService(OrderRepository repository,
                        CatalogueClient catalogueClient,
                        StockClient stockClient,
                        OrderMapperFull orderMapperFull) {
        this.repository = repository;
        this.catalogueClient = catalogueClient;
        this.stockClient = stockClient;
        this.orderMapperFull = orderMapperFull;
    }

    public List<OrderDTO> getAllOrders() {
        return repository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public OrderDTO getOrderById(Long id) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'id : " + id));
        return mapToDTO(order);
    }

    // --- CREATE LOGIC (With Feign checks) ---
    public OrderDTO createOrder(OrderDTO dto, String idempotencyKey) {

        // 1. Check Idempotency
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Clé d'idempotence manquante");
        }
        if (repository.findByIdempotencyKey(idempotencyKey).isPresent()) {
            throw new IllegalArgumentException("Opération déjà traitée avec cette clé");
        }

        Order order = new Order();
        order.setClientEmail(dto.getClientEmail());
        order.setDate(LocalDateTime.now());
        order.setStatut("EN_ATTENTE");
        order.setIdempotencyKey(idempotencyKey);

        BigDecimal total = BigDecimal.ZERO;

        for (OrderLineDTO lineDto : dto.getLines()) {

            // 2. Call Stock Service
            StockDTO stock = stockClient.getStockByProductId(lineDto.getProductId());
            // Note: Adjust getQuantite() if your StockDTO uses a different name (e.g., getQteStock())
            if (stock == null || stock.getQuantite() < lineDto.getQuantite()) {
                throw new IllegalArgumentException("Stock insuffisant pour le produit ID: " + lineDto.getProductId());
            }

            // 3. Call Catalogue Service
            ProductDTO product = catalogueClient.getProductById(lineDto.getProductId());
            if (product == null) {
                throw new IllegalArgumentException("Produit introuvable ID: " + lineDto.getProductId());
            }

            // 4. Create the line
            OrderLine line = new OrderLine();
            line.setProductId(lineDto.getProductId());
            line.setQuantite(lineDto.getQuantite());

            // Use price from Catalogue
            BigDecimal realPrice = product.getPrix();
            line.setPrixUnitaire(realPrice);
            line.setOrder(order);
            order.getLines().add(line);

            // Calculate total
            total = total.add(realPrice.multiply(new BigDecimal(lineDto.getQuantite())));
        }

        order.setMontantTotal(total);
        Order saved = repository.save(order);
        return mapToDTO(saved);
    }

    // --- ENRICHED GET LOGIC (For /detail endpoint) ---
    public OrderDTOFull getOrderFullById(Long id) {
        // 1. Load local order
        Order order = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'id : " + id));

        // 2. Map to Full DTO using the mapper
        OrderDTOFull dtoFull = orderMapperFull.toDto(order);

        // 3. Enrich with Feign (Catalogue Service)
        for (OrderLineDTOFull line : dtoFull.getLines()) {
            try {
                ProductDTO product = catalogueClient.getProductById(line.getProductId());
                if (product != null) {
                    line.setProduitNom(product.getNom());
                    line.setProduitDescription(product.getDescription());
                }
            } catch (Exception e) {
                // Fallback if catalogue service is down
                line.setProduitNom("Produit Inconnu (Service Catalogue indisponible)");
            }
        }
        return dtoFull;
    }

    // --- BASIC MAPPER (For normal endpoints) ---
    private OrderDTO mapToDTO(Order entity) {
        OrderDTO dto = new OrderDTO();
        dto.setId(entity.getId());
        dto.setClientEmail(entity.getClientEmail());
        dto.setDate(entity.getDate());
        dto.setStatut(entity.getStatut());
        dto.setMontantTotal(entity.getMontantTotal());

        List<OrderLineDTO> lines = entity.getLines().stream().map(l -> {
            OrderLineDTO ldto = new OrderLineDTO();
            ldto.setProductId(l.getProductId());
            ldto.setQuantite(l.getQuantite());
            ldto.setPrixUnitaire(l.getPrixUnitaire());
            return ldto;
        }).collect(Collectors.toList());

        dto.setLines(lines);
        return dto;
    }
}