package com.example.demo.communication.chat.message;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {

    private String content;
    private String sender;
    private String roomId;

}
