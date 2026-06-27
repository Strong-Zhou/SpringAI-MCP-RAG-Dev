
# 🔥 SpringBoot集成SpringAI保姆级教程

大家好，我是Lagogo！今天给大家带来一篇超详细的SpringBoot集成SpringAI教程，手把手带你实现AI聊天功能。

## 🚀 一、什么是SpringAI？

SpringAI是Spring官方推出的AI应用开发框架，它提供了统一的API来与各种AI模型进行交互，包括OpenAI、DeepSeek、Azure OpenAI等。使用SpringAI，我们可以轻松地在SpringBoot项目中集成AI能力，无需关注底层的HTTP调用细节。

## 📋 二、环境要求

| 工具 | 版本要求 |
| :--- | :--- |
| JDK | 17+ |
| SpringBoot | 3.5.x |
| SpringAI | 1.0.0 |

## 🔧 三、项目搭建

### 3.1 创建Maven项目

首先创建一个标准的SpringBoot Maven项目，这里使用SpringBoot 3.5.0版本。

### 3.2 配置父POM

在根目录的`pom.xml`中配置SpringBoot父依赖：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <packaging>pom</packaging>
    <modules>
        <module>mcp-client</module>
    </modules>

    <groupId>com.zhou</groupId>
    <artifactId>SpringAI-MCP-RAG-Dev</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.0</version>
        <relativePath/>
    </parent>
</project>
```

### 3.3 添加SpringAI依赖

在子模块`mcp-client`的`pom.xml`中添加SpringAI相关依赖：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.zhou</groupId>
        <artifactId>SpringAI-MCP-RAG-Dev</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <artifactId>mcp-client</artifactId>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

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
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-model-openai</artifactId>
        </dependency>
    </dependencies>
</project>
```

**关键点说明：**

1. **BOM管理**：通过`spring-ai-bom`管理所有SpringAI相关依赖的版本，避免版本冲突
2. **OpenAI Starter**：`spring-ai-starter-model-openai`不仅支持OpenAI，也兼容DeepSeek等兼容OpenAI API的模型

## ⚙️ 四、配置文件

在`application.yml`中配置DeepSeek的API信息：

```yaml
spring:
  application:
    name: spring-ai-mcp-client
  profiles:
    active: test
  ai:
    openai:
      api-key: ${DEEPSEEK_API_KEY}
      base-url: https://api.deepseek.com
      chat:
        options:
          model: deepseek-chat
```

**安全提示：**

⚠️ **绝对不要将API Key硬编码在代码中！** 使用环境变量`${DEEPSEEK_API_KEY}`是最佳实践，在启动时通过以下方式传入：

```bash
# Linux/Mac
export DEEPSEEK_API_KEY=your_api_key_here
java -jar mcp-client-1.0-SNAPSHOT.jar

# Windows PowerShell
$env:DEEPSEEK_API_KEY="your_api_key_here"
java -jar mcp-client-1.0-SNAPSHOT.jar
```

### 多环境配置

创建`application-test.yml`用于测试环境：

```yaml
server:
  port: 8090
```

创建`application-prod.yml`用于生产环境：

```yaml
server:
  port: 9999
```

## 🛠️ 五、编写代码

### 5.1 创建启动类

```java
package com.zhou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 5.2 创建Service层

首先定义接口：

```java
package com.zhou.service;

public interface ChatService {
    String chat(String prompt);
}
```

实现类：

```java
package com.zhou.service.impl;

import com.zhou.service.ChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

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
}
```

### 5.3 创建Controller层

```java
package com.zhou.controller;

import com.zhou.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
```

## 🚀 六、启动测试

启动应用后，访问以下接口测试：

**健康检查：**

```bash
curl http://localhost:8090/hello
# 返回：hello world
```

**同步聊天：**

```bash
curl "http://localhost:8090/prompt?prompt=你好"
# 返回AI的完整回复
```

## 💡 七、核心知识点

### 7.1 ChatClient的作用

`ChatClient`是SpringAI提供的核心客户端，它封装了与AI模型的交互逻辑，提供了简洁的API：

- `prompt(prompt)`：设置用户提示词
- `call()`：同步调用，等待完整响应
- `stream()`：流式调用，返回响应式流
- `content()`：提取响应内容

### 7.2 系统提示词

通过`defaultSystem(system)`可以设置系统提示词，用于定义AI助手的角色和行为模式。

## ⚠️ 八、踩坑总结

1. **API Key安全**：永远不要将API Key提交到代码仓库，使用环境变量或配置中心管理
2. **JDK版本**：SpringAI需要JDK 17+，低于17版本会报错
3. **BOM版本**：确保`spring-ai-bom`版本与SpringBoot版本兼容
4. **依赖冲突**：使用BOM管理版本，避免手动指定SpringAI子模块版本

## 📝 九、总结

通过以上步骤，我们成功实现了SpringBoot集成SpringAI的基础功能。核心步骤包括：

1. 添加SpringAI BOM和OpenAI Starter依赖
2. 配置API Key和模型信息
3. 注入ChatClient进行调用
4. 提供HTTP接口供外部调用

下一篇文章我将带大家实现**流式输出**功能，让AI回复像ChatGPT一样逐字显示！

---

**源码地址**：[SpringAI-MCP-RAG-Dev](https://github.com/your-repo/SpringAI-MCP-RAG-Dev)

如果觉得文章对你有帮助，欢迎点赞收藏！关注我，获取更多AI开发实战教程~
