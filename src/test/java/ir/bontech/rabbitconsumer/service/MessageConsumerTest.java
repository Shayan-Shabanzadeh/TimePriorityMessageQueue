package ir.bontech.rabbitconsumer.service;

import ir.bontech.rabbitconsumer.dto.MessageDto;
import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
class MessageConsumerTest {

    private MessageConsumer messageConsumer;


    @Container
    static RabbitMQContainer rabbitContainer;

    @BeforeAll
    static void setUpContainer() {
        rabbitContainer = new RabbitMQContainer("rabbitmq:3.8-management-alpine")
                .withReuse(true);
        rabbitContainer.start(); // Start the RabbitMQ container

        // Dynamically get the container's host and port
        String rabbitHost = rabbitContainer.getHost();
        int rabbitPort = rabbitContainer.getMappedPort(5672); // RabbitMQ default port

        // Set the properties for Spring RabbitMQ to use the container's host and port
        System.setProperty("spring.rabbitmq.host", rabbitHost);
        System.setProperty("spring.rabbitmq.port", String.valueOf(rabbitPort));
    }

    @BeforeEach
    void setUp() {
        messageConsumer = new MessageConsumer(5); // Create an instance of the actual class
        messageConsumer.init(); // Initialize the messageConsumer
    }

    @Test
    void testNoMessagesToProcess() throws InterruptedException {
        messageConsumer.startConsumingMessages();
        Thread.sleep(1000); // Let the consumer run for a while
        messageConsumer.stopConsumingMessages();
    }

    @Test
    void testProcessSingleMessage() {
        MessageDto message = new MessageDto("1", "content", System.currentTimeMillis(), 3);
        messageConsumer.handleMessage(message);
        messageConsumer.startConsumingMessages();
        // Wait for a while
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        messageConsumer.stopConsumingMessages();
        // Assert or verify as needed
    }


    @Test
    void testMessageProcessingBasedOnPriority() throws InterruptedException {
        var currentTime = Instant.now().toEpochMilli();
        // Create a spy of the actual MessageConsumer instance
        MessageConsumer messageConsumer = spy(new MessageConsumer(1));

        // Mock the PriorityBlockingQueue
        PriorityBlockingQueue<MessageDto> messageQueueMock = mock(PriorityBlockingQueue.class);
        doReturn(messageQueueMock).when(messageConsumer).getMessageQueue();

        // Initialize the messageConsumer
        messageConsumer.init();

        // Simulate sending mock messages with varying priorities
        MessageDto message1 = new MessageDto("1", "content1", currentTime, 3);
        MessageDto message2 = new MessageDto("2", "content2", currentTime, 2);
        MessageDto message3 = new MessageDto("3", "content3", currentTime, 4);


        // Simulate calling handleMessage for the actual instance
        messageConsumer.handleMessage(message1);
        messageConsumer.handleMessage(message2);
        messageConsumer.handleMessage(message3);

        // Wait for processing to complete
        TimeUnit.SECONDS.sleep(5);

        // Verify the interactions
        verify(messageConsumer, times(1)).init();
        verify(messageConsumer, times(1)).startConsumingMessages();

        // Verify that processMessages was invoked with the captured messages
        ArgumentCaptor<List<MessageDto>> argumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(messageConsumer, times(1)).processMessages(argumentCaptor.capture());

        // Assert that the captured messages match the expected order
        List<MessageDto> capturedMessages = argumentCaptor.getValue();
        List<MessageDto> expectedOrder = List.of(message3, message1, message2);
        assertEquals(expectedOrder, capturedMessages);
    }

    @Test
    void testProcessMultipleMessages() throws InterruptedException {
        MessageDto message1 = new MessageDto("1", "content1", System.currentTimeMillis(), 3);
        MessageDto message2 = new MessageDto("2", "content2", System.currentTimeMillis(), 2);
        MessageDto message3 = new MessageDto("3", "content3", System.currentTimeMillis(), 4);

        messageConsumer.handleMessage(message1);
        messageConsumer.handleMessage(message2);
        messageConsumer.handleMessage(message3);

        messageConsumer.startConsumingMessages();
        Thread.sleep(1000); // Let the consumer run for a while
        messageConsumer.stopConsumingMessages();
        // Assert or verify as needed
    }
}
