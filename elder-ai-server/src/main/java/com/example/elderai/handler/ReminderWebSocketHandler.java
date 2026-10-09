package com.example.elderai.handler;

import com.alibaba.fastjson2.JSON;
import com.example.elderai.entity.Reminder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class ReminderWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ReminderWebSocketHandler.class);

    private final Map<Long, CopyOnWriteArraySet<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = extractUserId(session);
        if (userId != null) {
            userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
            log.info("用户 [{}] 建立 WebSocket 连接，sessionId={}", userId, session.getId());
        } else {
            log.warn("无法识别用户，关闭连接: {}", session.getId());
            session.close(CloseStatus.POLICY_VIOLATION);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = extractUserId(session);
        if (userId != null) {
            CopyOnWriteArraySet<WebSocketSession> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
            log.info("用户 [{}] 关闭 WebSocket 连接，sessionId={}", userId, session.getId());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.debug("收到 WebSocket 消息: {}", message.getPayload());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket 传输错误: {}", exception.getMessage());
        Long userId = extractUserId(session);
        if (userId != null) {
            CopyOnWriteArraySet<WebSocketSession> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(session);
            }
        }
    }

    public void sendReminderToUser(Long userId, Reminder reminder) {
        CopyOnWriteArraySet<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            log.debug("用户 [{}] 没有在线连接，跳过推送", userId);
            return;
        }

        Map<String, Object> payload = Map.of(
                "type", "REMINDER_DUE",
                "reminder", Map.of(
                        "id", reminder.getId(),
                        "title", reminder.getTitle(),
                        "content", reminder.getContent(),
                        "remindTime", reminder.getRemindTime().toString(),
                        "remindType", reminder.getRemindType(),
                        "repeatType", reminder.getRepeatType()
                ),
                "timestamp", LocalDateTime.now().toString()
        );

        String json = JSON.toJSONString(payload);
        log.info("向用户 [{}] 推送提醒: {}", userId, reminder.getTitle());

        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                } else {
                    sessions.remove(session);
                }
            } catch (IOException e) {
                log.error("推送消息失败，sessionId={}: {}", session.getId(), e.getMessage());
                sessions.remove(session);
            }
        }
    }

    /** 向指定登录用户推送通用业务事件（SOS、预警等）。 */
    public void sendEventToUser(Long userId, String type, Map<String, Object> data) {
        CopyOnWriteArraySet<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) return;
        Map<String, Object> payload = Map.of(
                "type", type,
                "data", data,
                "timestamp", LocalDateTime.now().toString()
        );
        String json = JSON.toJSONString(payload);
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) session.sendMessage(new TextMessage(json));
                else sessions.remove(session);
            } catch (IOException e) {
                sessions.remove(session);
                log.warn("WebSocket event push failed, userId={}, type={}", userId, type);
            }
        }
    }

    private Long extractUserId(WebSocketSession session) {
        Object userIdObj = session.getAttributes().get("userId");
        if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        }
        return null;
    }
}
