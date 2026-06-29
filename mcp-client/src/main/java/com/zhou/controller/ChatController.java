package com.zhou.controller;

import com.zhou.entity.MsgEntity;
import com.zhou.service.ChatService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * Hello控制器 - 提供基础的HTTP接口服务
 * 包含同步和流式两种响应方式
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    /**
     * 聊天服务，用于处理对话请求
     */
    @Autowired
    private ChatService chatService;

    @PostMapping("/doChat")
    public void doChat(@RequestBody MsgEntity msgEntity) {
        chatService.doChat(msgEntity);
    }
}
