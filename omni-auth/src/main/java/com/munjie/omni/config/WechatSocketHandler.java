package com.munjie.omni.config;

import com.munjie.omni.exception.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WechatSocketHandler extends TextWebSocketHandler {

    private static final Map<String, WebSocketSession> sceneToSession = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri().getQuery();
        if (query != null && query.startsWith("scene=")) {
            String scene = query.substring("scene=".length());
            sceneToSession.put(scene, session);
            System.out.println("WebSocket connected for scene: " + scene);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sceneToSession.values().removeIf(s -> s.getId().equals(session.getId()));
    }

    public static void sendMessage(String scene, String message) {
        WebSocketSession session = sceneToSession.get(scene);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                throw new CustomException(e.getMessage());
            }
        }
    }
}