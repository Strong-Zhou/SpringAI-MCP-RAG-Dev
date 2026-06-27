package com.zhou.controller;

import com.zhou.service.ChatService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * Hello控制器 - 提供基础的HTTP接口服务
 * 包含同步和流式两种响应方式
 */
@RestController
public class HelloController {

    /**
     * 聊天服务，用于处理对话请求
     */
    @Autowired
    ChatService chatService;

    /**
     * 基础健康检查接口
     * 用于验证应用是否正常运行
     *
     * @return 固定的问候字符串 "hello world"
     */
    @GetMapping("/hello")
    public String hello() {
        return "hello world";
    }

    /**
     * 同步聊天接口
     * 接收用户提示词，返回完整的聊天响应（阻塞式）
     *
     * @param propmt 用户的提示词/问题
     * @return AI生成的完整回复内容
     */
    @GetMapping("/propmt")
    public String propmt(String propmt) {
        String chat = chatService.chat(propmt);
        return chat;
    }

    /**
     * 流式聊天接口
     * 支持Server-Sent Events (SSE) 流式响应，逐字或逐块返回AI生成的内容
     * 适用于需要实时显示生成进度的场景
     *
     * @param propmt 用户的提示词/问题
     * @param response HTTP响应对象，用于设置字符编码为UTF-8以支持中文
     * @return 响应式流（Flux），包含逐步生成的AI回复内容
     */
    @GetMapping("/propmt/flux")
    public Flux<String> propmtFlux(String propmt, HttpServletResponse response) {
        response.setCharacterEncoding("utf-8");
        return chatService.chatFlux(propmt);
    }
}
