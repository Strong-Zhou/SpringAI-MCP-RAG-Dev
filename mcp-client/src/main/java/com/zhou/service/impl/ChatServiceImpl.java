package com.zhou.service.impl;

import cn.hutool.json.JSONUtil;
import com.zhou.entity.MsgEntity;
import com.zhou.entity.MsgEntityResponse;
import com.zhou.enums.SSEMsgType;
import com.zhou.service.ChatService;
import com.zhou.utils.SSEServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
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
        return client.prompt(propmt).stream().content();
    }


    @Override
    public void doChat(MsgEntity msgEntity) {
        String userId = msgEntity.getCurrentUserName();
        String prompt = msgEntity.getMessage();
        String botMsgId = msgEntity.getBotMsgId();

        Flux<String> stringFlux = client.prompt(prompt).stream().content();


        List<String> contents =stringFlux.toStream().map(chatResponse->{
            String content = chatResponse.toString();
            SSEServer.sendMsg(userId, content, SSEMsgType.ADD);
            log.info("用户: {}, 响应: {}", userId, content);
            return content;
        }).collect(Collectors.toList());

        String fullText = contents.stream().collect(Collectors.joining());

        MsgEntityResponse msgEntityResponse = new MsgEntityResponse(fullText, botMsgId);

        SSEServer.sendMsg(userId, JSONUtil.toJsonStr(msgEntityResponse), SSEMsgType.FINISH);
    }
}
