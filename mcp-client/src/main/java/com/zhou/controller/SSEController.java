package com.zhou.controller;

import com.zhou.enums.SSEMsgType;
import com.zhou.utils.SSEServer;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/sse")
public class SSEController {

    @GetMapping(value = "/connect",produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    public SseEmitter connect(@RequestParam String userId) {
        SseEmitter connect = null;
        try {
            connect = SSEServer.connect(userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return connect;
    }

    // localhost:8090/sse/sendMsg?userId=n1xvc632d&msg=hello
    @GetMapping("/sendMsg")
    public void sendMsg(@RequestParam String userId,@RequestParam  String msg) {
        SSEServer.sendMsg(userId, msg, SSEMsgType.MESSAGE);
    }

}
