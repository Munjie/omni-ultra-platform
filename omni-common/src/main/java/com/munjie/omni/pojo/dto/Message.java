package com.munjie.omni.pojo.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Message {

    private String role;    // "user" 或 "assistant"
    private String content;
}
