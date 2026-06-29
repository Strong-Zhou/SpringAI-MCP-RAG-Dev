package com.zhou.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MsgEntity {

    private String currentUserName;
    private String message;
    private String botMsgId;

}
