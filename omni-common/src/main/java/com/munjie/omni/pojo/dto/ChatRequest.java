package com.munjie.omni.pojo.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatRequest {

    private List<Message> messages;
    private String apiKey;
}
