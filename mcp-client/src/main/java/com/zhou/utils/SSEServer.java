package com.zhou.utils;

import com.zhou.enums.SSEMsgType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * SSE（Server-Sent Events）服务器工具类
 * 用于管理客户端与服务器的长连接，支持实时消息推送
 */
@Slf4j
public class SSEServer {
    
    /**
     * 存储所有活跃的SSE连接
     * Key: 用户ID
     * Value: SseEmitter对象或相关数据
     */
    public static ConcurrentHashMap<String,SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * 创建SSE连接
     * @param userId 用户唯一标识
     * @return SseEmitter对象，用于向客户端推送消息
     */
    public static SseEmitter connect(String userId) {
        // 创建SSE连接，超时时间设置为0表示永不超时
        SseEmitter sseEmitter = new SseEmitter(0L);
        
        // 注册回调方法：超时、完成、异常
        sseEmitter.onTimeout(timeout(userId));
        sseEmitter.onCompletion(complete(userId));
        sseEmitter.onError((error(userId)));
        log.info("SSE连接创建成功，用户ID: {}", userId);

        // 将连接保存到集合中，便于后续管理和推送
        emitters.put(userId, sseEmitter);
        
        // 发送连接成功的初始消息
        try {
            sseEmitter.send(SseEmitter.event()
                    .name("connected")
                    .data("连接成功，用户ID: " + userId));
            log.info("已发送连接成功消息，用户ID: {}", userId);
        } catch (IOException e) {
            log.error("发送初始消息失败，用户ID: {}", userId, e);
        }
        
        return sseEmitter;
    }

    /**
     * 发送消息给指定用户
     * @param userId 用户ID
     * @param message 要发送的消息内容
     * @param msgType 消息类型
     */
    public static void sendMsg(String userId, String message, SSEMsgType msgType) {
        log.info("准备发送消息，用户ID: {}, 消息: {}, 类型: {}", userId, message, msgType.type);
        
        if(!emitters.containsKey(userId)) {
            log.warn("用户不存在或未连接，用户ID: {}", userId);
            return;
        }
        
        SseEmitter sseEmitter = emitters.get(userId);
        if (sseEmitter == null) {
            log.warn("SseEmitter为空，用户ID: {}", userId);
            return;
        }
        
        sendEmitterMsg(sseEmitter, userId, message, msgType);
    }

    /**
     * 发送消息给指定用户
     * @param userId 用户ID
     * @param message 要发送的消息内容
     * @param msgType 消息类型
     */
    public static void sendMsgAll(String userId, String message, SSEMsgType msgType) {
        log.info("准备发送消息，用户ID: {}, 消息: {}, 类型: {}", userId, message, msgType.type);

        if(!emitters.containsKey(userId)) {
            log.warn("用户不存在或未连接，用户ID: {}", userId);
            return;
        }

        SseEmitter sseEmitter = emitters.get(userId);
        if (sseEmitter == null) {
            log.warn("SseEmitter为空，用户ID: {}", userId);
            return;
        }

        sendEmitterMsg(sseEmitter, userId, message, msgType);
    }

    /**
     * 发送消息给指定用户
     * @param sseEmitter SSE发射器
     * @param userId 用户ID
     * @param message 要发送的消息内容
     * @param msgType 消息类型
     */
    private static void sendEmitterMsg(SseEmitter sseEmitter, String userId, String message, SSEMsgType msgType) {
        SseEmitter.SseEventBuilder msgEvent = SseEmitter.event()
                .id(userId)
                .data(message)
                .name(msgType.type);
        
        try {
            sseEmitter.send(msgEvent);
            log.info("✅ 消息发送成功，用户ID: {}, 消息类型: {}", userId, msgType.type);
        } catch (IOException e) {
            log.error("❌ 消息发送失败，用户ID: {}, 错误: {}", userId, e.getMessage());
            remove(userId);
        }
    }

    /**
     * 创建超时回调
     * @param userId 用户ID
     * @return 超时处理逻辑
     */
    private static Runnable timeout(String userId) {
        return () -> {
            log.info("SSE连接超时，用户ID: {}", userId);
            remove(userId);
        };
    }

    /**
     * 移除指定用户的SSE连接记录
     * @param userId 用户ID
     */
    public static void remove(String userId) {
        emitters.remove(userId);
        log.info("已移除用户连接，用户ID: {}, 当前活跃连接数: {}", userId, emitters.size());
    }

    /**
     * 创建异常回调
     * @param userId 用户ID
     * @return 异常处理逻辑
     */
    private static Consumer<Throwable> error(String userId) {
        return throwable -> {
            log.error("SSE连接异常，用户ID: {}, 错误: {}", userId, throwable.getMessage());
            remove(userId);
        };
    }

    /**
     * 创建完成回调
     * @param userId 用户ID
     * @return 完成处理逻辑
     */
    private static Runnable complete(String userId) {
        return () -> {
            log.info("SSE连接完成，用户ID: {}", userId);
            remove(userId);
        };
    }

}
