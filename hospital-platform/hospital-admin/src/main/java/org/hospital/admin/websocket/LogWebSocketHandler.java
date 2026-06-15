package org.hospital.admin.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.hospital.admin.service.LogTailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * 日志 WebSocket 消息处理器
 * 处理客户端控制指令：start/stop/switch/pause/resume
 */
@Slf4j
@Component
public class LogWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private LogTailService logTailService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket 日志连接建立: {}", session.getId());
        sendInfo(session, "已连接，发送 {\"action\":\"start\",\"file\":\"hospital-platform.log\"} 开始追踪");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            JsonNode json = objectMapper.readTree(message.getPayload());
            String action = json.has("action") ? json.get("action").asText() : "";
            String file = json.has("file") ? json.get("file").asText() : "";

            switch (action) {
                case "start" -> {
                    if (file.isEmpty()) {
                        sendError(session, "缺少 file 参数");
                        return;
                    }
                    logTailService.startTailing(session, file);
                }
                case "stop" -> logTailService.stopTailing(session.getId());
                case "switch" -> {
                    if (file.isEmpty()) {
                        sendError(session, "缺少 file 参数");
                        return;
                    }
                    logTailService.switchFile(session, file);
                }
                case "pause" -> logTailService.pauseTailing(session.getId());
                case "resume" -> logTailService.resumeTailing(session.getId());
                default -> sendError(session, "未知操作: " + action);
            }
        } catch (Exception e) {
            log.warn("处理 WebSocket 消息失败: {}", e.getMessage());
            sendError(session, "消息格式错误");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket 日志连接关闭: {}, 状态: {}", session.getId(), status);
        logTailService.stopTailing(session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn("WebSocket 传输错误: {}", exception.getMessage());
        logTailService.stopTailing(session.getId());
    }

    private void sendInfo(WebSocketSession session, String message) {
        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("type", "info");
            msg.put("message", message);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
        } catch (Exception e) {
            log.debug("发送 info 消息失败");
        }
    }

    private void sendError(WebSocketSession session, String message) {
        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("type", "error");
            msg.put("message", message);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
        } catch (Exception e) {
            log.debug("发送 error 消息失败");
        }
    }
}
