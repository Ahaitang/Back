package org.hospital.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日志实时追踪服务
 * 使用 RandomAccessFile 追踪文件末尾变化，定时推送新内容
 */
@Slf4j
@Service
public class LogTailService {

    @Autowired
    private LogFileService logFileService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, TailState> sessions = new ConcurrentHashMap<>();

    /**
     * 开始追踪日志文件
     */
    public void startTailing(WebSocketSession session, String filename) {
        Path filePath = logFileService.resolveAndValidate(filename);

        TailState state = new TailState();
        state.session = session;
        state.filename = filename;
        state.filePath = filePath;
        state.paused = false;

        // 初始化文件指针到末尾，先发送最后 100 行
        try {
            List<String> lastLines = readLastLines(filePath, 100);
            state.filePointer = Files.size(filePath);
            sessions.put(session.getId(), state);

            sendLogLines(session, lastLines, filename);
        } catch (IOException e) {
            log.error("启动日志追踪失败: {}", filename, e);
            sendError(session, "启动追踪失败: " + e.getMessage());
        }
    }

    /**
     * 停止追踪
     */
    public void stopTailing(String sessionId) {
        sessions.remove(sessionId);
    }

    /**
     * 切换追踪文件
     */
    public void switchFile(WebSocketSession session, String filename) {
        stopTailing(session.getId());
        startTailing(session, filename);
    }

    /**
     * 暂停追踪
     */
    public void pauseTailing(String sessionId) {
        TailState state = sessions.get(sessionId);
        if (state != null) {
            state.paused = true;
        }
    }

    /**
     * 恢复追踪
     */
    public void resumeTailing(String sessionId) {
        TailState state = sessions.get(sessionId);
        if (state != null) {
            state.paused = false;
        }
    }

    /**
     * 定时检查文件变化并推送新内容
     */
    @Scheduled(fixedDelay = 500)
    public void pollNewLines() {
        for (Iterator<Map.Entry<String, TailState>> it = sessions.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, TailState> entry = it.next();
            TailState state = entry.getValue();

            if (!state.session.isOpen()) {
                it.remove();
                continue;
            }

            if (state.paused) {
                continue;
            }

            try {
                long fileLength = Files.size(state.filePath);
                if (fileLength < state.filePointer) {
                    // 文件被截断（日志轮转），重置指针
                    state.filePointer = 0;
                }

                if (fileLength > state.filePointer) {
                    List<String> newLines = readNewLines(state);
                    if (!newLines.isEmpty()) {
                        sendLogLines(state.session, newLines, state.filename);
                    }
                }
            } catch (IOException e) {
                log.debug("追踪日志文件失败: {}", state.filename);
            }
        }
    }

    private List<String> readNewLines(TailState state) throws IOException {
        List<String> lines = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(state.filePath.toFile(), "r")) {
            raf.seek(state.filePointer);
            String line;
            while ((line = raf.readLine()) != null) {
                lines.add(new String(line.getBytes("ISO-8859-1"), "UTF-8"));
            }
            state.filePointer = raf.getFilePointer();
        }
        return lines;
    }

    private List<String> readLastLines(Path filePath, int count) throws IOException {
        List<String> allLines = Files.readAllLines(filePath);
        int start = Math.max(0, allLines.size() - count);
        return new ArrayList<>(allLines.subList(start, allLines.size()));
    }

    private void sendLogLines(WebSocketSession session, List<String> lines, String filename) {
        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("type", "log");
            msg.put("lines", lines);
            msg.put("file", filename);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
        } catch (IOException e) {
            log.debug("发送日志消息失败: {}", e.getMessage());
        }
    }

    private void sendError(WebSocketSession session, String message) {
        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("type", "error");
            msg.put("message", message);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
        } catch (IOException e) {
            log.debug("发送错误消息失败: {}", e.getMessage());
        }
    }

    private static class TailState {
        WebSocketSession session;
        String filename;
        Path filePath;
        long filePointer;
        boolean paused;
    }
}
