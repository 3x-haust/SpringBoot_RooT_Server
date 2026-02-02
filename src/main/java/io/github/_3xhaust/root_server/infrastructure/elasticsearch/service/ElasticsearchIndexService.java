package io.github._3xhaust.root_server.infrastructure.elasticsearch.service;

import io.github._3xhaust.root_server.domain.garagesale.entity.GarageSale;
import io.github._3xhaust.root_server.domain.product.entity.Product;
import io.github._3xhaust.root_server.domain.tag.service.TagService;
import io.github._3xhaust.root_server.infrastructure.elasticsearch.document.GarageSaleDocument;
import io.github._3xhaust.root_server.infrastructure.elasticsearch.document.ProductDocument;
import io.github._3xhaust.root_server.infrastructure.elasticsearch.repository.GarageSaleSearchRepository;
import io.github._3xhaust.root_server.infrastructure.elasticsearch.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchIndexService {

    private final ProductSearchRepository productSearchRepository;
    private final GarageSaleSearchRepository garageSaleSearchRepository;
    private final TagService tagService;

    public void indexProduct(Product product) {
        try {
            List<String> tags = tagService.getProductTags(product.getId());
            List<String> imageUrls = product.getProductImages().stream()
                    .map(img -> img.getImage().getUrl())
                    .toList();

            Double latitude = product.getLatitude();
            Double longitude = product.getLongitude();

            if (latitude == null && product.getGarageSale() != null) {
                latitude = product.getGarageSale().getLatitude();
                longitude = product.getGarageSale().getLongitude();
            }

            ProductDocument.ProductDocumentBuilder builder = ProductDocument.builder()
                    .id(ProductDocument.generateId(product.getId()))
                    .productId(product.getId())
                    .sellerId(product.getSeller().getId())
                    .sellerUsername(product.getSeller().getUsername())
                    .title(product.getTitle())
                    .price(product.getPrice())
                    .description(product.getDescription())
                    .type(product.getType())
                    .garageSaleId(product.getGarageSale() != null ? product.getGarageSale().getId() : null)
                    .tags(tags)
                    .imageUrls(imageUrls)
                    .createdAt(product.getCreatedAt())
                    .isActive(true)
                    .latitude(latitude)
                    .longitude(longitude);

            if (latitude != null && longitude != null) {
                builder.location(ProductDocument.GeoPoint.builder()
                        .lat(latitude)
                        .lon(longitude)
                        .build());
            }

            ProductDocument document = builder.build();

            productSearchRepository.save(document);
            log.info("Product indexed: {}", product.getId());
        } catch (Exception e) {
            log.error("Failed to index product: {}", product.getId(), e);
        }
    }

    public void deleteProductIndex(Long productId) {
        try {
            productSearchRepository.deleteById(ProductDocument.generateId(productId));
            log.info("Product index deleted: {}", productId);
        } catch (Exception e) {
            log.error("Failed to delete product index: {}", productId, e);
        }
    }

    public void deactivateProduct(Long productId) {
        try {
            productSearchRepository.findById(ProductDocument.generateId(productId))
                    .ifPresent(doc -> {
                        ProductDocument updated = ProductDocument.builder()
                                .id(doc.getId())
                                .productId(doc.getProductId())
                                .sellerId(doc.getSellerId())
                                .sellerUsername(doc.getSellerUsername())
                                .title(doc.getTitle())
                                .price(doc.getPrice())
                                .description(doc.getDescription())
                                .type(doc.getType())
                                .garageSaleId(doc.getGarageSaleId())
                                .latitude(doc.getLatitude())
                                .longitude(doc.getLongitude())
                                .location(doc.getLocation())
                                .tags(doc.getTags())
                                .imageUrls(doc.getImageUrls())
                                .createdAt(doc.getCreatedAt())
                                .isActive(false)
                                .build();
                        productSearchRepository.save(updated);
                    });
            log.info("Product deactivated: {}", productId);
        } catch (Exception e) {
            log.error("Failed to deactivate product: {}", productId, e);
        }
    }

    public void indexGarageSale(GarageSale garageSale) {
        try {
            List<String> tags = tagService.getGarageSaleTags(garageSale.getId());

            GarageSaleDocument document = GarageSaleDocument.builder()
                    .id(GarageSaleDocument.generateId(garageSale.getId()))
                    .garageSaleId(garageSale.getId())
                    .ownerId(garageSale.getOwner().getId())
                    .ownerName(garageSale.getOwner().getName())
                    .ownerEmail(garageSale.getOwner().getEmail())
                    .name(garageSale.getName())
                    .latitude(garageSale.getLatitude())
                    .longitude(garageSale.getLongitude())
                    .location(GarageSaleDocument.GeoPoint.builder()
                            .lat(garageSale.getLatitude())
                            .lon(garageSale.getLongitude())
                            .build())
                    .startDate(garageSale.getStartDate())
                    .endDate(garageSale.getEndDate())
                    .startTime(convertToInstant(garageSale.getStartDate(), garageSale.getStartTime()))
                    .endTime(convertToInstant(garageSale.getEndDate(), garageSale.getEndTime()))
                    .tags(tags)
                    .productCount(garageSale.getProducts().size())
                    .createdAt(garageSale.getCreatedAt())
                    .isActive(true)
                    .build();

            garageSaleSearchRepository.save(document);
            log.info("GarageSale indexed: {}", garageSale.getId());
        } catch (Exception e) {
            log.error("Failed to index garage sale: {}", garageSale.getId(), e);
        }
    }

    public void deleteGarageSaleIndex(Long garageSaleId) {
        try {
            garageSaleSearchRepository.deleteById(GarageSaleDocument.generateId(garageSaleId));
            log.info("GarageSale index deleted: {}", garageSaleId);
        } catch (Exception e) {
            log.error("Failed to delete garage sale index: {}", garageSaleId, e);
        }
    }

    public void deactivateGarageSale(Long garageSaleId) {
        try {
            garageSaleSearchRepository.findById(GarageSaleDocument.generateId(garageSaleId))
                    .ifPresent(doc -> {
                        GarageSaleDocument updated = GarageSaleDocument.builder()
                                .id(doc.getId())
                                .garageSaleId(doc.getGarageSaleId())
                                .ownerId(doc.getOwnerId())
                                .ownerName(doc.getOwnerName())
                                .ownerEmail(doc.getOwnerEmail())
                                .name(doc.getName())
                                .latitude(doc.getLatitude())
                                .longitude(doc.getLongitude())
                                .location(doc.getLocation())
                                .startDate(doc.getStartDate())
                                .endDate(doc.getEndDate())
                                .startTime(doc.getStartTime())
                                .endTime(doc.getEndTime())
                                .tags(doc.getTags())
                                .productCount(doc.getProductCount())
                                .createdAt(doc.getCreatedAt())
                                .isActive(false)
                                .build();
                        garageSaleSearchRepository.save(updated);
                    });
            log.info("GarageSale deactivated: {}", garageSaleId);
        } catch (Exception e) {
            log.error("Failed to deactivate garage sale: {}", garageSaleId, e);
        }
    }

    private Instant convertToInstant(LocalDate date, LocalTime time) {
        if (date == null || time == null) {
            return Instant.now();
        }
        return date.atTime(time).atZone(ZoneId.systemDefault()).toInstant();
    }
}

