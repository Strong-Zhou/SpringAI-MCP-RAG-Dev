package com.zhou.service;

import com.zhou.entity.MsgEntity;
import reactor.core.publisher.Flux;

public interface ChatService {

    String chat(String propmt);

    Flux<String> chatFlux(String propmt);

    void doChat(MsgEntity msgEntity);
}
