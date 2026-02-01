package io.github._3xhaust.root_server.domain.chatmessage.entity;

import io.github._3xhaust.root_server.domain.chatroom.entity.ChatRoom;
import io.github._3xhaust.root_server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Entity
@Table(name = "chat_messages", indexes = {
    @Index(name = "idx_chat_room_created_at", columnList = "chat_room_id, created_at DESC")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    public ChatMessage(ChatRoom chatRoom, User sender, String message) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.message = message;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
