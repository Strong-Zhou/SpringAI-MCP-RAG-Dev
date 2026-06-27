package com.zhou.service;

import reactor.core.publisher.Flux;

public interface ChatService {

    String chat(String propmt);

    Flux<String> chatFlux(String propmt);
}
