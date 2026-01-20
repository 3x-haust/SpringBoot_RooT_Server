package io.github._3xhaust.root_server.infrastructure.elasticsearch.service;

import io.github._3xhaust.root_server.infrastructure.elasticsearch.document.GarageSaleDocument;
import io.github._3xhaust.root_server.infrastructure.elasticsearch.document.ProductDocument;
import io.github._3xhaust.root_server.infrastructure.elasticsearch.repository.GarageSaleSearchRepository;
import io.github._3xhaust.root_server.infrastructure.elasticsearch.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ElasticsearchTagService {

    private final ProductSearchRepository productSearchRepository;
    private final GarageSaleSearchRepository garageSaleSearchRepository;

    public List<String> getPopularGarageSaleTags(int limit) {
        Map<String, Long> allTagCounts = new HashMap<>();
        Map<String, Long> recentTagCounts = new HashMap<>();

        Pageable pageable = PageRequest.of(0, 10000);
        Instant sevenDaysAgo = Instant.now().minusSeconds(7 * 24 * 60 * 60);

        Page<GarageSaleDocument> garageSales = garageSaleSearchRepository.findByIsActiveTrue(pageable);
        for (GarageSaleDocument doc : garageSales.getContent()) {
            if (doc.getTags() != null) {
                for (String tag : doc.getTags()) {
                    allTagCounts.put(tag, allTagCounts.getOrDefault(tag, 0L) + 1);
                    if (doc.getCreatedAt() != null && doc.getCreatedAt().isAfter(sevenDaysAgo)) {
                        recentTagCounts.put(tag, recentTagCounts.getOrDefault(tag, 0L) + 1);
                    }
                }
            }
        }

        return allTagCounts.keySet().stream()
                .map(tag -> {
                    long usageCount = allTagCounts.getOrDefault(tag, 0L);
                    long recentUsageCount = recentTagCounts.getOrDefault(tag, 0L);
                    long score = usageCount * 2 + recentUsageCount * 3;
                    return new AbstractMap.SimpleEntry<>(tag, score);
                })
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<String> getPopularProductTags(int limit) {
        Map<String, Long> allTagCounts = new HashMap<>();
        Map<String, Long> recentTagCounts = new HashMap<>();

        Pageable pageable = PageRequest.of(0, 10000);
        Instant sevenDaysAgo = Instant.now().minusSeconds(7 * 24 * 60 * 60);

        Page<ProductDocument> products = productSearchRepository.findByIsActiveTrue(pageable);
        for (ProductDocument doc : products.getContent()) {
            if (doc.getTags() != null) {
                for (String tag : doc.getTags()) {
                    allTagCounts.put(tag, allTagCounts.getOrDefault(tag, 0L) + 1);
                    if (doc.getCreatedAt() != null && doc.getCreatedAt().isAfter(sevenDaysAgo)) {
                        recentTagCounts.put(tag, recentTagCounts.getOrDefault(tag, 0L) + 1);
                    }
                }
            }
        }

        return allTagCounts.keySet().stream()
                .map(tag -> {
                    long usageCount = allTagCounts.getOrDefault(tag, 0L);
                    long recentUsageCount = recentTagCounts.getOrDefault(tag, 0L);
                    long score = usageCount * 2 + recentUsageCount * 3;
                    return new AbstractMap.SimpleEntry<>(tag, score);
                })
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
