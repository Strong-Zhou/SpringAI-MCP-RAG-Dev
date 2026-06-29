package com.zhou.service.impl;

import com.zhou.service.ChatService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatServiceImpl implements ChatService {

    private ChatClient client;

    private final String system = "你是一个解答大师，你的名字叫Lagogo";

    public ChatServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.client = chatClientBuilder
                        //.defaultSystem(system)
                        .build();
    }
    @Override
    public String chat(String propmt) {
        String content = client.prompt(propmt).call().content();
        return content;
    }

    @Override
    public Flux<String> chatFlux(String propmt) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return client.prompt(propmt).stream().content();
    }
}
