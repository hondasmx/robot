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
    private static final short MAXIMUM_MESSAGE_LENGTH = 4096;

    @Value("${telegram.token}")
    private String token;

    @Value("${telegram.chat-id}")
    private String chatId;

    public void sendMessage(String message) {
        var endpoint = String.format("https://api.telegram.org/bot%s/sendMessage", token);
        unirestClient.doPostRequest(endpoint, new TelegramMessage(sanitize(message), chatId));
    }

    //0-4096 символов
    private String sanitize(String message) {
        if (message.length() > MAXIMUM_MESSAGE_LENGTH) {
            return message.substring(0, MAXIMUM_MESSAGE_LENGTH);
        }
        return message;
    }

    @Data
    @RequiredArgsConstructor
    public static class TelegramMessage {
        private final String text;

        private final String chat_id;

        private final String parse_mode = "markdown";

    }
}
