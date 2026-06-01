package com.munjie.omni.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class EmailTask implements Serializable {
    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${system.email}")
    private String mailbox;

    public void sendEmail(SimpleMailMessage message){
        message.setFrom(mailbox);
        // 抄送给自己
        message.setCc(mailbox);
        javaMailSender.send(message);
    }
}
