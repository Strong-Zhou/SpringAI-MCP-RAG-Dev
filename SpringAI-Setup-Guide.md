# Spring Boot 3.x + Spring AI 实战：快速搭建 AI 聊天应用

> Spring AI 1.0.0 正式发布，5 分钟上手搭建同步+流式双模式聊天应用

---

## 快速开始

### 技术栈

| 技术 | 版本 |
|:---:|:---:|
| Java | 17 |
| Spring Boot | 3.5.0 |
| Spring AI | 1.0.0 |
| DeepSeek | - |

### 创建项目

```
SpringAI-MCP-RAG-Dev/
├── pom.xml
└── mcp-client/
    ├── pom.xml
    └── src/main/
        ├── java/com/zhou/
        │   ├── Application.java
        │   ├── controller/HelloController.java
        │   ├── service/ChatService.java
        │   └── service/impl/ChatServiceImpl.java
        └── resources/application.yml
```

---

## 第一步：配置依赖

**父工程 pom.xml：**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.0</version>
    </parent>

    <groupId>com.zhou</groupId>
    <artifactId>SpringAI-MCP-RAG-Dev</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    <modules>
        <module>mcp-client</module>
    </modules>
</project>
```

**子模块 pom.xml：**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>com.zhou</groupId>
        <artifactId>SpringAI-MCP-RAG-Dev</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <artifactId>mcp-client</artifactId>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.ai</groupId>
                <artifactId>spring-ai-bom</artifactId>
                <version>1.0.0</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-model-openai</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
</project>
```

---

## 第二步：配置 AI 服务

**application.yml：**

```yaml
spring:
  application:
    name: spring-ai-mcp-client
  ai:
    openai:
      api-key: sk-xxx                      # 替换为你的 API Key
      base-url: https://api.deepseek.com  # DeepSeek API
      chat:
        options:
          model: deepseek-chat

server:
  port: 8090
```

> **获取 API Key**：前往 [platform.deepseek.com](https://platform.deepseek.com) 注册获取。
> **切换模型**：只需修改 `base-url` 和 `model`，兼容 OpenAI、豆包、通义千问等。

---

## 第三步：实现聊天服务

**ChatService.java：**

```java
package com.zhou.service;

import reactor.core.publisher.Flux;

public interface ChatService {
    String chat(String prompt);
    Flux<String> chatFlux(String prompt);
}
```

**ChatServiceImpl.java：**

```java
package com.zhou.service.impl;

import com.zhou.service.ChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatClient client;

    public ChatServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.client = chatClientBuilder.build();
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

| 方法 | 说明 |
|:---|:---|
| `.call()` | 同步调用，返回完整响应 |
| `.stream()` | 流式调用，逐字返回（SSE） |

---

## 第四步：创建 REST 接口

**HelloController.java：**

```java
package com.zhou.controller;

import com.zhou.service.ChatService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/chat")
    public String chat(String prompt) {
        return chatService.chat(prompt);
    }

    @GetMapping("/chat/flux")
    public Flux<String> chatFlux(String prompt, HttpServletResponse response) {
        response.setCharacterEncoding("utf-8");
        return chatService.chatFlux(prompt);
    }
}
```

---

## 第五步：启动测试

```bash
cd mcp-client
mvn spring-boot:run
```

**测试同步接口：**
```bash
curl "http://localhost:8090/chat?prompt=你好"
```

**测试流式接口：**
```bash
curl "http://localhost:8090/chat/flux?prompt=写一首诗"
```

---

## 可选：AOP 性能监控

**LogAspect.java：**

```java
package com.zhou.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class LogAspect {

    @Around("execution(* com.zhou.service.*.*(..))")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long cost = System.currentTimeMillis() - start;
        
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        
        if (cost > 2000) {
            log.error("慢请求: {}#{} 耗时 {} ms", className, methodName, cost);
        }
        
        return proceed;
    }
}
```

> 添加依赖：`spring-boot-starter-aop`

---

## 扩展：支持更多模型

| 模型 | 依赖 |
|:---|:---|
| OpenAI | `spring-ai-starter-model-openai` |
| Anthropic | `spring-ai-starter-model-anthropic` |
| Google Gemini | `spring-ai-starter-model-gemini` |

---

## 总结

- ✅ 引入 Spring AI BOM 管理版本
- ✅ 配置 `base-url` 对接任意 OpenAI 兼容模型
- ✅ `.call()` 同步调用 / `.stream()` 流式调用
- ✅ SSE 流式接口需设置 `utf-8` 编码

完整代码可直接运行，拿来即用！