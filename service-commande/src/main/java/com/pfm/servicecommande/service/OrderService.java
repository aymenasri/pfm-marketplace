package com.pfm.servicecommande.service;

import com.pfm.servicecommande.dto.OrderDTO;
import com.pfm.servicecommande.dto.OrderDTOFull;
import com.pfm.servicecommande.dto.OrderLineDTO;
import com.pfm.servicecommande.dto.OrderLineDTOFull;
import com.pfm.servicecommande.dto.NotificationEmailDto;
import com.pfm.servicecommande.entity.Order;
import com.pfm.servicecommande.entity.OrderLine;
import com.pfm.servicecommande.config.RabbitConfig;
import com.pfm.servicecommande.exception.BusinessRuleException;
import com.pfm.servicecommande.exception.ResourceNotFoundException;
import com.pfm.servicecommande.exception.DownstreamServiceException;
import com.pfm.servicecommande.feignClient.StockClient;
import com.pfm.servicecommande.feignClient.ProductDTO;
import com.pfm.servicecommande.feignClient.StockDTO;
import com.pfm.servicecommande.mapper.OrderMapperFull;
import com.pfm.servicecommande.repository.OrderRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository repository;
    private final CatalogueProductService catalogueProductService;
    private final StockClient stockClient;
    private final OrderMapperFull orderMapperFull;
    private final RabbitTemplate rabbitTemplate;

    public OrderService(OrderRepository repository,
                        CatalogueProductService catalogueProductService,
                        StockClient stockClient,
                        OrderMapperFull orderMapperFull,
                        RabbitTemplate rabbitTemplate) {
        this.repository = repository;
        this.catalogueProductService = catalogueProductService;
        this.stockClient = stockClient;
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

    @Transactional
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
            StockDTO stock = getStock(lineDto.getProductId());
            if (stock == null || stock.getQuantite() < lineDto.getQuantite()) {
                throw new BusinessRuleException("Stock insuffisant pour le produit ID: " + lineDto.getProductId());
            }

            ProductDTO product = catalogueProductService.getForOrder(lineDto.getProductId());
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
        scheduleOrderNotification(saved);
        return mapToDTO(saved);
    }

    public OrderDTOFull getOrderFullById(Long id) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'id : " + id));

        OrderDTOFull dtoFull = orderMapperFull.toDto(order);

        for (OrderLineDTOFull line : dtoFull.getLines()) {
            ProductDTO product = catalogueProductService.getForRead(line.getProductId());
            line.setProduitNom(product.getNom());
            line.setProduitDescription(product.getDescription());
            StockDTO stock = getStock(line.getProductId());
            line.setSousTotal(line.getPrixUnitaire()
                    .multiply(BigDecimal.valueOf(line.getQuantite())));
        }
        return dtoFull;
    }

    private StockDTO getStock(Long productId) {
        try {
            StockDTO stock = stockClient.getStockByProductId(productId);
            if (stock == null) {
                throw new ResourceNotFoundException("Stock introuvable pour le produit ID: " + productId);
            }
            return stock;
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Stock introuvable pour le produit ID: " + productId);
        } catch (FeignException e) {
            throw new DownstreamServiceException("Service stock indisponible");
        }
    }

    private void scheduleOrderNotification(Order order) {
        NotificationEmailDto notification = new NotificationEmailDto(
                order.getClientEmail(),
                "Confirmation de votre commande #" + order.getId(),
                "Votre commande #" + order.getId()
                        + " d'un montant de " + order.getMontantTotal()
                        + " a été enregistrée.");

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    rabbitTemplate.convertAndSend(RabbitConfig.QUEUE_EMAIL, notification);
                } catch (AmqpException e) {
                    log.error("Commande {} enregistrée mais notification non publiée", order.getId(), e);
                }
            }
        });
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