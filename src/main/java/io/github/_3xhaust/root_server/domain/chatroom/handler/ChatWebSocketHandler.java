package io.github._3xhaust.root_server.domain.chatroom.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github._3xhaust.root_server.domain.chatroom.dto.ChatMessageDTO;
import io.github._3xhaust.root_server.domain.chatroom.service.ChatService;
import io.github._3xhaust.root_server.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final ChatService chatService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String PENDING_MESSAGES_PREFIX = "chat:pending:";
    private static final String ACTIVE_SESSIONS_PREFIX = "chat:active:";
    private static final Duration MESSAGE_TTL = Duration.ofHours(24);
    private static final Duration SESSION_TTL = Duration.ofHours(1);

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (command == StompCommand.CONNECT) {
            String token = getTokenFromHeader(accessor);
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                String name = jwtTokenProvider.getNameFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(name);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                accessor.setUser(authentication);
                
                String sessionId = accessor.getSessionId();
                if (sessionId != null) {
                    String key = ACTIVE_SESSIONS_PREFIX + sessionId;
                    redisTemplate.opsForValue().set(key, name, SESSION_TTL);
                }
            }
        } else if (command == StompCommand.DISCONNECT) {
            Principal principal = accessor.getUser();
            if (principal != null) {
                String sessionId = accessor.getSessionId();
                if (sessionId != null) {
                    try {
                        List<ChatMessageDTO> messages = getPendingMessages(sessionId);
                        if (messages != null && !messages.isEmpty()) {
                            chatService.saveMessages(messages);
                            deletePendingMessages(sessionId);
                        }
                        deleteActiveSession(sessionId);
                    } catch (Exception e) {
                        log.error("Failed to save messages on disconnect", e);
                    }
                }
            }
        }

        return message;
    }

    private String getTokenFromHeader(StompHeaderAccessor accessor) {
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String bearerToken = authHeaders.get(0);
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            }
        }
        return null;
    }

    public void addPendingMessage(String sessionId, ChatMessageDTO message) {
        try {
            String key = PENDING_MESSAGES_PREFIX + sessionId;
            String messageJson = objectMapper.writeValueAsString(message);
            redisTemplate.opsForList().rightPush(key, messageJson);
            redisTemplate.expire(key, MESSAGE_TTL);
        } catch (Exception e) {
            log.error("Failed to add pending message to Redis", e);
        }
    }

    private List<ChatMessageDTO> getPendingMessages(String sessionId) {
        try {
            String key = PENDING_MESSAGES_PREFIX + sessionId;
            List<String> messageJsonList = redisTemplate.opsForList().range(key, 0, -1);
            if (messageJsonList == null || messageJsonList.isEmpty()) {
                return new ArrayList<>();
            }

            List<ChatMessageDTO> messages = new ArrayList<>();
            for (String messageJson : messageJsonList) {
                ChatMessageDTO message = objectMapper.readValue(messageJson, ChatMessageDTO.class);
                messages.add(message);
            }
            return messages;
        } catch (Exception e) {
            log.error("Failed to get pending messages from Redis", e);
            return new ArrayList<>();
        }
    }

    private void deletePendingMessages(String sessionId) {
        try {
            String key = PENDING_MESSAGES_PREFIX + sessionId;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Failed to delete pending messages from Redis", e);
        }
    }

    private void deleteActiveSession(String sessionId) {
        try {
            String key = ACTIVE_SESSIONS_PREFIX + sessionId;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Failed to delete active session from Redis", e);
        }
    }
}
