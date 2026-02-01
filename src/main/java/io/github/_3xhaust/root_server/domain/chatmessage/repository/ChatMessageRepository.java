package io.github._3xhaust.root_server.domain.chatmessage.repository;

import io.github._3xhaust.root_server.domain.chatmessage.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId, Pageable pageable);
    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);
}
