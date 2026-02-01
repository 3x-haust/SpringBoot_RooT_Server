package io.github._3xhaust.root_server.domain.chatroom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDTO {
    private Long id;
    private Long productId;
    private String productTitle;
    private Long sellerId;
    private String sellerName;
    private Long buyerId;
    private String buyerName;
    private Instant createdAt;
}
