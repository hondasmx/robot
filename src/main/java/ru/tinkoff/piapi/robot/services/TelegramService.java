package ru.tinkoff.piapi.robot.services;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.robot.services.rest.UnirestClient;

@Service
@RequiredArgsConstructor
public class TelegramService {

    private final UnirestClient unirestClient;

    @Value("${telegram.token}")
    private String token;

    @Value("${telegram.chat-id}")
    private String chatId;

    public void sendMessage(String message) {
        var endpoint = String.format("https://api.telegram.org/bot%s/sendMessage", token);
        unirestClient.doPostRequest(endpoint, new TelegramMessage(message, chatId));
    }

    @Data
    @RequiredArgsConstructor
    public static class TelegramMessage {
        private final String text;

        private final String chat_id;

    }
}
