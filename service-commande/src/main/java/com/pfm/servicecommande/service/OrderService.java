package com.pfm.servicecommande.service;

import com.pfm.servicecommande.dto.OrderDTO;
import com.pfm.servicecommande.dto.OrderDTOFull;
import com.pfm.servicecommande.dto.OrderLineDTO;
import com.pfm.servicecommande.dto.OrderLineDTOFull;
import com.pfm.servicecommande.entity.Order;
import com.pfm.servicecommande.entity.OrderLine;
import com.pfm.servicecommande.exception.ResourceNotFoundException;
import com.pfm.servicecommande.exception.BusinessRuleException;
import com.pfm.servicecommande.dto.NotificationEmailDto;
import com.pfm.servicecommande.config.RabbitConfig;
import com.pfm.servicecommande.feignClient.ProductDTO;
import com.pfm.servicecommande.feignClient.ResilientRequests;
import com.pfm.servicecommande.feignClient.StockDTO;
import com.pfm.servicecommande.mapper.OrderMapperFull;
import com.pfm.servicecommande.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.annotation.Transactional;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository repository;
    private final ResilientRequests resilientRequests; // Replaced Clients
    private final OrderMapperFull orderMapperFull;
    private final RabbitTemplate rabbitTemplate;

    public OrderService(OrderRepository repository,
                        ResilientRequests resilientRequests, // Replaced Clients
                        OrderMapperFull orderMapperFull,
                        RabbitTemplate rabbitTemplate) {
        this.repository = repository;
        this.resilientRequests = resilientRequests;
        this.orderMapperFull = orderMapperFull;
        this.rabbitTemplate = rabbitTemplate;
    }

    public boolean isIdempotencyKeyProcessed(String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey).isPresent();
    }

    public List<OrderDTO> getAllOrders() {
        return repository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public OrderDTO getOrderById(Long id) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'id : " + id));
        return mapToDTO(order);
    }

    public synchronized OrderDTO createOrder(OrderDTO dto, String idempotencyKey) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Clé d'idempotence manquante");
        }
        var existing = repository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return mapToDTO(existing.get());
        }
        if (dto.getLines() == null || dto.getLines().isEmpty()) {
            throw new BusinessRuleException("La commande doit contenir au moins une ligne");
        }

        Order order = new Order();
        order.setClientEmail(dto.getClientEmail());
        order.setDate(LocalDateTime.now());
        order.setStatut("EN_ATTENTE");
        order.setIdempotencyKey(idempotencyKey);

        BigDecimal total = BigDecimal.ZERO;

        for (OrderLineDTO lineDto : dto.getLines()) {
            // STRICT METHODS FOR CREATION
            StockDTO stock = resilientRequests.getStockByIdStrict(lineDto.getProductId());
            if (stock == null || stock.getQuantite() < lineDto.getQuantite()) {
                throw new BusinessRuleException("Stock insuffisant pour le produit ID: " + lineDto.getProductId());
            }

            ProductDTO product = resilientRequests.getProduitByIdStrict(lineDto.getProductId());
            if (product == null) {
                throw new ResourceNotFoundException("Produit introuvable ID: " + lineDto.getProductId());
            }

            OrderLine line = new OrderLine();
            line.setProductId(lineDto.getProductId());
            line.setQuantite(lineDto.getQuantite());

            BigDecimal realPrice = product.getPrix();
            line.setPrixUnitaire(realPrice);
            line.setOrder(order);
            order.getLines().add(line);

            total = total.add(realPrice.multiply(new BigDecimal(lineDto.getQuantite())));
        }

        order.setMontantTotal(total);
        Order saved = repository.save(order);
        publishOrderNotification(saved);
        return mapToDTO(saved);
    }

    // ADDED RATE LIMITER HERE
    @RateLimiter(name = "consultation", fallbackMethod = "consultationLimitee")
    @Transactional(readOnly = true)
    public OrderDTOFull getOrderFullById(Long id) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'id : " + id));

        OrderDTOFull dtoFull = orderMapperFull.toDto(order);

        for (OrderLineDTOFull line : dtoFull.getLines()) {
            // FALLBACK METHODS FOR READING (FB)
            ProductDTO product = resilientRequests.getProduitByIdFb(line.getProductId());
            if (product != null) {
                line.setProduitNom(product.getNom());
                line.setProduitDescription(product.getDescription());
            }

            StockDTO stock = resilientRequests.getStockByIdFb(line.getProductId());

            line.setSousTotal(line.getPrixUnitaire()
                    .multiply(BigDecimal.valueOf(line.getQuantite())));
        }
        return dtoFull;
    }

    // FALLBACK FOR RATE LIMITER
    public OrderDTOFull consultationLimitee(Long id, RequestNotPermitted cause) {
        log.warn("Rate limit reached for enriched consultation. Returning local data only.");
        Order order = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'id : " + id));
        return orderMapperFull.toDto(order); // Returns without enriching
    }

    private void publishOrderNotification(Order order) {
        NotificationEmailDto notification = new NotificationEmailDto(
                order.getClientEmail(),
                "Confirmation de votre commande #" + order.getId(),
                "Votre commande #" + order.getId()
                        + " d'un montant de " + order.getMontantTotal()
                        + " a été enregistrée.");
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.QUEUE_EMAIL, notification);
        } catch (AmqpException e) {
            log.error("Commande {} enregistrée mais notification non publiée", order.getId(), e);
        }
    }

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