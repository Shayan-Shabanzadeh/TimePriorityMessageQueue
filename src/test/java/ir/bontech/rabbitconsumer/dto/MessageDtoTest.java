package ir.bontech.rabbitconsumer.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
class MessageDtoTest {

    Instant instant = Instant.now();
    long currentTime = instant.toEpochMilli();


    @Value("${messageDto.priorityIncreaseTimeWindowSeconds:60}")
    private int priorityIncreaseTimeWindowSeconds;


    @Test
    void compareTo_HigherPriorityAndRecentTimestamp_ShouldComeFirst() {
        MessageDto higherPriorityMessage = MessageDto.builder()
                .id("1")
                .content("higherPriority")
                .creationTimestamp(currentTime - 5 * 60 * 1000) // 5 minutes ago
                .priority(5)
                .priorityIncreaseTimeWindowSeconds(priorityIncreaseTimeWindowSeconds)
                .build();

        MessageDto lowerPriorityMessage = MessageDto.builder()
                .id("2")
                .content("lowerPriority")
                .creationTimestamp(currentTime - 2 * 60 * 1000) // 2 minutes ago
                .priority(2)
                .priorityIncreaseTimeWindowSeconds(priorityIncreaseTimeWindowSeconds)
                .build();

        int result = higherPriorityMessage.compareTo(lowerPriorityMessage);

        assertTrue(result < 0, "Higher priority message should come first");
    }


    @Test
    void compareTo_equal() {
        MessageDto message1 = MessageDto.builder()
                .id("1")
                .content("message1")
                .creationTimestamp(currentTime) // 5 minutes ago
                .priority(5)
                .priorityIncreaseTimeWindowSeconds(priorityIncreaseTimeWindowSeconds).build();

        MessageDto message2 = MessageDto.builder()
                .id("2")
                .content("message2")
                .creationTimestamp(currentTime) // 2 minutes ago
                .priority(5)
                .priorityIncreaseTimeWindowSeconds(priorityIncreaseTimeWindowSeconds).build();

        int result = message1.compareTo(message2);

        assertTrue(result == 0, "Higher priority message should come first");
    }

    @Test
    void testMessageProcessingBasedOnPriority() {
        MessageDto message1 = MessageDto.builder()
                .id("1")
                .content("content1")
                .creationTimestamp(currentTime - 4 * 60 * 1000) // 4 minutes ago
                .priority(3)
                .priorityIncreaseTimeWindowSeconds(priorityIncreaseTimeWindowSeconds).build();

        MessageDto message2 = MessageDto.builder()
                .id("2")
                .content("content2")
                .creationTimestamp(currentTime - 3 * 60 * 1000) // 3 minutes ago
                .priority(2)
                .priorityIncreaseTimeWindowSeconds(priorityIncreaseTimeWindowSeconds).build();

        MessageDto message3 = MessageDto.builder()
                .id("3")
                .content("content3")
                .creationTimestamp(currentTime - 2 * 60 * 1000) // 2 minutes ago
                .priority(4)
                .priorityIncreaseTimeWindowSeconds(priorityIncreaseTimeWindowSeconds).build();

        int result = message1.compareTo(message2);
        assertTrue(result < 0, "Message with higher priority should come first");

        result = message2.compareTo(message3);
        assertTrue(result > 0, "Message with earlier timestamp should come first");
    }


    @Test
    void compareTo_equal_creation_time_different_priority() {
        MessageDto lowerPriorityMessage = MessageDto.builder()
                .id("1")
                .content("message1")
                .creationTimestamp(currentTime) // 5 minutes ago
                .priority(4)
                .priorityIncreaseTimeWindowSeconds(priorityIncreaseTimeWindowSeconds).build();

        MessageDto higherPriorityMessage = MessageDto.builder()
                .id("2")
                .content("message2")
                .creationTimestamp(currentTime) // 2 minutes ago
                .priority(5)
                .priorityIncreaseTimeWindowSeconds(priorityIncreaseTimeWindowSeconds).build();

        int result = lowerPriorityMessage.compareTo(higherPriorityMessage);

        assertTrue(result > 0, "Higher priority message should come first");
    }

    @Test
    void compareTo_LowerPriorityAndRecentTimestamp_ShouldComeSecond() {
        MessageDto lowerPriorityMessage = MessageDto.builder()
                .id("1")
                .content("lowerPriority")
                .creationTimestamp(currentTime - 10 * 60 * 1000) // 5 minutes ago
                .priority(2)
                .priorityIncreaseTimeWindowSeconds(priorityIncreaseTimeWindowSeconds).build();

        MessageDto higherPriorityMessage = MessageDto.builder()
                .id("2")
                .content("higherPriority")
                .creationTimestamp(currentTime - 2 * 60 * 1000) // 2 minutes ago
                .priority(3)
                .priorityIncreaseTimeWindowSeconds(priorityIncreaseTimeWindowSeconds).build();

        System.out.println("Message1: " + lowerPriorityMessage);
        System.out.println("Message2:" + higherPriorityMessage);

        int result = lowerPriorityMessage.compareTo(higherPriorityMessage);

        assertTrue(result < 0, "Lower priority message should come second");
    }

    @Test
    void compareTo_SamePriorityAndRecentTimestamp_ShouldCompareBasedOnTimestamp() {
        MessageDto firstMessage = MessageDto.builder()
                .id("1")
                .content("first")
                .creationTimestamp(currentTime - 4 * 60 * 1000) // 2 minutes ago
                .priority(3)
                .priorityIncreaseTimeWindowSeconds(priorityIncreaseTimeWindowSeconds).build();

        MessageDto secondMessage = MessageDto.builder()
                .id("2")
                .content("second")
                .creationTimestamp(currentTime - 60 * 1000) // 1 minute ago
                .priority(3)
                .priorityIncreaseTimeWindowSeconds(priorityIncreaseTimeWindowSeconds).build();

        int result = firstMessage.compareTo(secondMessage);

        assertTrue(result < 0, "Message with earlier timestamp should come first");
    }

    @Test
    void compareTo_LowerPriorityAndOlderTimestamp_ShouldComeFirst() {
        MessageDto lowerPriorityMessage = MessageDto.builder()
                .id("1")
                .content("lowerPriority")
                .creationTimestamp(currentTime - 5 * 60 * 1000) // 5 minutes ago
                .priority(2)
                .priorityIncreaseTimeWindowSeconds(priorityIncreaseTimeWindowSeconds).build();

        System.out.println("");

        MessageDto higherPriorityMessage = MessageDto.builder()
                .id("2")
                .content("higherPriority")
                .creationTimestamp(currentTime - 60 * 1000) // 1 minutes ago
                .priority(4)
                .priorityIncreaseTimeWindowSeconds(priorityIncreaseTimeWindowSeconds).build();

        int result = lowerPriorityMessage.compareTo(higherPriorityMessage);

        assertTrue(result < 0, "Lower priority message with older timestamp should come first");
    }

    @Test
    void compareTo_HigherPriorityAndRecentTimestamp_ShouldComeSecond() {
        MessageDto higherPriorityMessage = MessageDto.builder()
                .id("1")
                .content("higherPriority")
                .creationTimestamp(currentTime)
                .priority(1000000000)
                .priorityIncreaseTimeWindowSeconds(priorityIncreaseTimeWindowSeconds).build();

        MessageDto lowerPriorityMessage = MessageDto.builder()
                .id("2")
                .content("lowerPriority")
                .creationTimestamp(currentTime - 60 * 1000)
                .priority(1)
                .priorityIncreaseTimeWindowSeconds(priorityIncreaseTimeWindowSeconds).build();

        int result = higherPriorityMessage.compareTo(lowerPriorityMessage);

        assertTrue(result < 0, "Higher priority message should come first");
    }
}

