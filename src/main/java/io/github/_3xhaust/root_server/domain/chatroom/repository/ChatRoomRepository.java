package io.github._3xhaust.root_server.domain.chatroom.repository;

import io.github._3xhaust.root_server.domain.chatroom.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByProductIdAndSellerIdAndBuyerId(Long productId, Long sellerId, Long buyerId);
    List<ChatRoom> findBySellerIdOrBuyerId(Long sellerId, Long buyerId);
}
