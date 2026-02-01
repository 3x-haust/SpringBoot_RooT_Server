package io.github._3xhaust.root_server.domain.chatroom.service;

import io.github._3xhaust.root_server.domain.chatmessage.entity.ChatMessage;
import io.github._3xhaust.root_server.domain.chatmessage.repository.ChatMessageRepository;
import io.github._3xhaust.root_server.domain.chatroom.dto.ChatMessageDTO;
import io.github._3xhaust.root_server.domain.chatroom.dto.ChatRoomDTO;
import io.github._3xhaust.root_server.domain.chatroom.entity.ChatRoom;
import io.github._3xhaust.root_server.domain.chatroom.repository.ChatRoomRepository;
import io.github._3xhaust.root_server.domain.product.entity.Product;
import io.github._3xhaust.root_server.domain.product.repository.ProductRepository;
import io.github._3xhaust.root_server.domain.user.entity.User;
import io.github._3xhaust.root_server.domain.user.exception.UserErrorCode;
import io.github._3xhaust.root_server.domain.user.exception.UserException;
import io.github._3xhaust.root_server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public ChatRoom getOrCreateChatRoom(Long productId, Long sellerId, Long buyerId) {
        return chatRoomRepository.findByProductIdAndSellerIdAndBuyerId(productId, sellerId, buyerId)
                .orElseGet(() -> {
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
                    User seller = userRepository.findById(sellerId)
                            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "id=" + sellerId));
                    User buyer = userRepository.findById(buyerId)
                            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "id=" + buyerId));

                    ChatRoom chatRoom = ChatRoom.builder()
                            .product(product)
                            .seller(seller)
                            .buyer(buyer)
                            .build();
                    return chatRoomRepository.save(chatRoom);
                });
    }

    @Transactional
    public void saveMessage(Long chatRoomId, Long senderId, String message) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("ChatRoom not found: " + chatRoomId));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "id=" + senderId));

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .message(message)
                .build();

        chatMessageRepository.save(chatMessage);
    }

    @Transactional
    public void saveMessages(List<ChatMessageDTO> messages) {
        for (ChatMessageDTO dto : messages) {
            saveMessage(dto.getChatRoomId(), dto.getSenderId(), dto.getMessage());
        }
    }

    public List<ChatRoomDTO> getChatRoomsByUserId(Long userId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findBySellerIdOrBuyerId(userId, userId);
        return chatRooms.stream()
                .map(chatRoom -> ChatRoomDTO.builder()
                        .id(chatRoom.getId())
                        .productId(chatRoom.getProduct().getId())
                        .productTitle(chatRoom.getProduct().getTitle())
                        .sellerId(chatRoom.getSeller().getId())
                        .sellerName(chatRoom.getSeller().getName())
                        .buyerId(chatRoom.getBuyer().getId())
                        .buyerName(chatRoom.getBuyer().getName())
                        .createdAt(chatRoom.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    public Page<ChatMessageDTO> getMessages(Long chatRoomId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId, pageable);
        return messages.map(message -> ChatMessageDTO.builder()
                .id(message.getId())
                .chatRoomId(message.getChatRoom().getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getName())
                .message(message.getMessage())
                .createdAt(message.getCreatedAt())
                .build());
    }
}
