
# 🌊 SpringAI集成DeepSeek流式输出实战

大家好，我是Lagogo！上一篇文章我们实现了SpringBoot集成SpringAI的基础功能，今天继续深入，带大家实现**流式输出**功能，让AI回复像ChatGPT一样逐字显示！

## 🚀 一、什么是流式输出？

传统的同步调用是等待AI生成完整回复后一次性返回，用户需要等待较长时间。而**流式输出**采用Server-Sent Events（SSE）技术，AI生成一段内容就立即返回一段，用户可以实时看到回复过程，大大提升用户体验。

## 📋 二、技术原理

### 2.1 SSE（Server-Sent Events）

SSE是一种服务器向客户端推送数据的技术，基于HTTP协议，特点：

- 单向通信：服务器→客户端
- 长连接：连接保持打开状态
- 文本格式：数据以文本形式传输，通常是JSON

### 2.2 Reactor响应式编程

SpringAI的流式输出基于Reactor框架，返回`Flux<String>`类型，表示一个异步的、可订阅的字符串序列。

## 🔧 三、实现步骤

### 3.1 完善Service层

首先在`ChatService`接口中添加流式方法：

```java
package com.zhou.service;

import reactor.core.publisher.Flux;

public interface ChatService {
    String chat(String prompt);
    Flux<String> chatFlux(String prompt);
}
```

在实现类中添加流式方法：

```java
package com.zhou.service.impl;

import com.zhou.service.ChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatClient client;

    private final String system = "你是一个解答大师，你的名字叫Lagogo";

    public ChatServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.client = chatClientBuilder
                        .defaultSystem(system)
                        .build();
    }

    @Override
    public String chat(String prompt) {
        return client.prompt(prompt).call().content();
    }

    @Override
    public Flux<String> chatFlux(String prompt) {
        return client.prompt(prompt).stream().content();
    }
}
```

### 3.2 创建流式Controller端点 ⚠️ 关键

这是最关键的一步！必须添加`produces = MediaType.TEXT_EVENT_STREAM_VALUE`注解：

```java
package com.zhou.controller;

import com.zhou.service.ChatService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class HelloController {

    @Autowired
    ChatService chatService;

    @GetMapping("/hello")
    public String hello() {
        return "hello world";
    }

    @GetMapping("/prompt")
    public String prompt(String prompt) {
        return chatService.chat(prompt);
    }

    @GetMapping(value = "/prompt/flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> promptFlux(String prompt, HttpServletResponse response) {
        response.setCharacterEncoding("utf-8");
        return chatService.chatFlux(prompt);
    }
}
```

**⚠️ 踩坑重点：**

如果没有添加`produces = MediaType.TEXT_EVENT_STREAM_VALUE`，SpringBoot会将`Flux<String>`视为普通响应，等待所有数据收集完成后一次性返回，完全失去了流式的意义！

## 🌐 四、前端接收演示

### 4.1 HTML页面

创建一个简单的HTML页面来演示流式接收：

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DeepSeek流式聊天</title>
    <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
            background: #f5f5f5;
        }
        .chat-container {
            background: white;
            border-radius: 12px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            padding: 20px;
            height: 500px;
            overflow-y: auto;
            margin-bottom: 20px;
        }
        .message {
            margin-bottom: 15px;
            padding: 12px 16px;
            border-radius: 8px;
        }
        .user-message {
            background: #007bff;
            color: white;
            text-align: right;
            margin-left: 100px;
        }
        .ai-message {
            background: #e9ecef;
            color: #333;
            margin-right: 100px;
            white-space: pre-wrap;
        }
        .input-area {
            display: flex;
            gap: 10px;
        }
        input {
            flex: 1;
            padding: 12px;
            border: 1px solid #ddd;
            border-radius: 8px;
            font-size: 16px;
        }
        button {
            padding: 12px 24px;
            background: #007bff;
            color: white;
            border: none;
            border-radius: 8px;
            cursor: pointer;
            font-size: 16px;
        }
        button:disabled {
            background: #6c757d;
            cursor: not-allowed;
        }
    </style>
</head>
<body>
    <div class="chat-container" id="chatContainer">
        <div class="message ai-message">你好！我是Lagogo，有什么可以帮你的？</div>
    </div>
    <div class="input-area">
        <input type="text" id="inputPrompt" placeholder="输入你的问题..." />
        <button id="sendBtn">发送</button>
    </div>

    <script>
        const chatContainer = document.getElementById('chatContainer');
        const inputPrompt = document.getElementById('inputPrompt');
        const sendBtn = document.getElementById('sendBtn');

        sendBtn.addEventListener('click', sendMessage);
        inputPrompt.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') sendMessage();
        });

        async function sendMessage() {
            const prompt = inputPrompt.value.trim();
            if (!prompt) return;

            // 添加用户消息
            addMessage(prompt, 'user');
            inputPrompt.value = '';
            sendBtn.disabled = true;

            // 添加AI消息容器
            const aiMessageDiv = addMessage('', 'ai');

            try {
                // 创建EventSource连接
                const url = `/prompt/flux?prompt=${encodeURIComponent(prompt)}`;
                const eventSource = new EventSource(url);

                eventSource.onmessage = (event) => {
                    if (event.data) {
                        aiMessageDiv.textContent += event.data;
                        chatContainer.scrollTop = chatContainer.scrollHeight;
                    }
                };

                eventSource.onerror = (error) => {
                    console.error('SSE Error:', error);
                    eventSource.close();
                    sendBtn.disabled = false;
                };

                eventSource.onclose = () => {
                    console.log('SSE Connection Closed');
                    sendBtn.disabled = false;
                };

            } catch (error) {
                console.error('Error:', error);
                aiMessageDiv.textContent = '请求失败，请重试';
                sendBtn.disabled = false;
            }
        }

        function addMessage(text, type) {
            const div = document.createElement('div');
            div.className = `message ${type}-message`;
            div.textContent = text;
            chatContainer.appendChild(div);
            chatContainer.scrollTop = chatContainer.scrollHeight;
            return div;
        }
    </script>
</body>
</html>
```

### 4.2 使用curl测试

```bash
curl -N "http://localhost:8090/prompt/flux?prompt=给我讲一个故事"
```

参数`-N`表示不缓冲输出，实时显示响应内容。

## 🔄 五、流式与同步对比

| 特性 | 同步调用 | 流式调用 |
| :--- | :--- | :--- |
| 等待时间 | 需等待完整响应 | 立即看到内容 |
| 用户体验 | 较差 | 流畅自然 |
| 返回类型 | `String` | `Flux<String>` |
| 响应头 | `Content-Type: application/json` | `Content-Type: text/event-stream` |

## ⚠️ 六、常见问题与解决方案

### 6.1 中文乱码问题

**问题**：流式返回中文出现乱码

**解决方案**：在Controller中设置字符编码：

```java
response.setCharacterEncoding("utf-8");
```

### 6.2 流式不生效

**问题**：返回的是完整内容，不是逐字输出

**解决方案**：检查是否添加了`produces = MediaType.TEXT_EVENT_STREAM_VALUE`注解

### 6.3 EventSource跨域问题

**问题**：前端页面和后端不在同一域名，EventSource连接失败

**解决方案**：在后端添加CORS配置：

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
```

### 6.4 连接超时问题

**问题**：长时间无响应时连接断开

**解决方案**：在前端添加重连机制：

```javascript
eventSource.onerror = (error) => {
    console.error('SSE Error:', error);
    eventSource.close();
    // 3秒后重连
    setTimeout(() => startStream(), 3000);
};
```

## 💡 七、进阶优化

### 7.1 批量输出

默认情况下，SpringAI每生成一个token就返回一次。可以通过配置实现批量输出，减少网络传输次数：

```java
@Override
public Flux<String> chatFlux(String prompt) {
    return client.prompt(prompt)
            .stream()
            .content()
            .bufferTimeout(10, Duration.ofMillis(100))
            .map(list -> String.join("", list));
}
```

### 7.2 添加心跳

防止长连接被网关或代理中断。注意：SSE协议中以冒号开头的行是注释，客户端会忽略，不会触发onmessage事件：

```java
@GetMapping(value = "/prompt/flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> promptFlux(String prompt, HttpServletResponse response) {
    response.setCharacterEncoding("utf-8");
    
    Flux<String> dataFlux = chatService.chatFlux(prompt);
    Flux<String> heartbeatFlux = Flux.interval(Duration.ofSeconds(15))
            .map(t -> ": heartbeat");
    
    return Flux.merge(dataFlux, heartbeatFlux);
}
```

## 📝 八、总结

通过本文，我们学会了：

1. **SSE技术原理**：服务器向客户端推送数据的机制
2. **关键注解**：`produces = MediaType.TEXT_EVENT_STREAM_VALUE`是流式输出的核心
3. **Flux响应式**：基于Reactor框架实现异步流式处理
4. **前端接收**：使用EventSource API接收流式数据
5. **常见问题**：中文乱码、跨域、连接超时等解决方案

## 📚 九、完整代码

完整代码已上传至GitHub，包含：

- ✅ SpringBoot集成SpringAI
- ✅ 同步调用和流式调用
- ✅ 多环境配置
- ✅ 前端流式演示页面

**源码地址**：[SpringAI-MCP-RAG-Dev](https://github.com/your-repo/SpringAI-MCP-RAG-Dev)

如果觉得文章对你有帮助，欢迎点赞收藏！关注我，获取更多AI开发实战教程~

---

**系列文章目录：**

1. 🔥 [SpringBoot集成SpringAI保姆级教程](https://juejin.cn/post/xxx)
2. 🌊 SpringAI集成DeepSeek流式输出实战（本文）
3. 📚 待续...
