package io.github._3xhaust.root_server.domain.history.service;

import io.github._3xhaust.root_server.domain.history.entity.SearchHistory;
import io.github._3xhaust.root_server.domain.history.entity.ViewHistory;
import io.github._3xhaust.root_server.domain.history.repository.SearchHistoryRepository;
import io.github._3xhaust.root_server.domain.history.repository.ViewHistoryRepository;
import io.github._3xhaust.root_server.domain.user.entity.User;
import io.github._3xhaust.root_server.domain.user.repository.UserRepository;
import io.github._3xhaust.root_server.domain.user.exception.UserErrorCode;
import io.github._3xhaust.root_server.domain.user.exception.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class HistoryService {
    private final ViewHistoryRepository viewHistoryRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final UserRepository userRepository;

    public void recordView(String name, Long garageSaleId, Long productId) {
        if (name == null) return;

        User user = userRepository.findByName(name)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "name=" + name));

        ViewHistory viewHistory = ViewHistory.builder()
                .user(user)
                .garageSaleId(garageSaleId)
                .productId(productId)
                .build();

        viewHistoryRepository.save(viewHistory);
    }

    public void recordSearch(String name, String keyword, Long garageSaleId, Long productId) {
        if (name == null || keyword == null || keyword.isBlank()) return;

        User user = userRepository.findByName(name)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "name=" + name));

        SearchHistory searchHistory = SearchHistory.builder()
                .user(user)
                .keyword(keyword)
                .garageSaleId(garageSaleId)
                .productId(productId)
                .build();

        searchHistoryRepository.save(searchHistory);
    }
}
