package ir.bontech.rabbitconsumer.uitls;

import ir.bontech.rabbitconsumer.dto.MessageDto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MessageGenerator {


    public static List<MessageDto> generateMessages(int count) {
        List<MessageDto> messages = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            MessageDto message = MessageDto.builder()
                    .id(String.valueOf(i + 1))
                    .content("Content " + (i + 1))
                    .creationTimestamp(Instant.now().toEpochMilli() - random.nextInt(3600000)) // Within the last hour
                    .priority(random.nextInt(10) + 1) // Priority between 1 and 10
                    .build();
            messages.add(message);
        }

        return messages;
    }
}

