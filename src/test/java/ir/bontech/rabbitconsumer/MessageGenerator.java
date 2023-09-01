package ir.bontech.rabbitconsumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.bontech.rabbitconsumer.dto.MessageDto;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MessageGenerator {


    private static List<MessageDto> generateMessages(int count) {
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

    public static void createAndWriteMessagesToJsonFile(String fileName) {
        List<MessageDto> messages = generateMessages(1000);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(new File(fileName), messages);
            System.out.println("Messages written to " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

