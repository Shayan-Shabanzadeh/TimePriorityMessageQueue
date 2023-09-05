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
            MessageDto message = new MessageDto(
                    String.valueOf(i + 1),
                    "Content " + (i + 1),
                    Instant.now().toEpochMilli() - random.nextInt(3600000),
                    random.nextInt(10) + 1);
            messages.add(message);
        }

        return messages;
    }
}

