package ir.bontech.rabbitconsumer.service;

import ir.bontech.rabbitconsumer.dto.MessageDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class MessageConsumerTest {

    private ExecutorService executorService;
    private MessageConsumer messageConsumer;
    private List<MessageDto> processedMessages;

    @BeforeEach
    void setUp() {
        messageConsumer = new MessageConsumer(); // Create an instance of the actual class
        messageConsumer.init(); // Initialize the messageConsumer
    }

    @AfterEach
    void tearDown() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    @Test
    void testNoMessagesToProcess() throws InterruptedException {
        messageConsumer.startConsumingMessages();
        Thread.sleep(1000); // Let the consumer run for a while
        messageConsumer.stopConsumingMessages();
        // Assert or verify as needed
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
        // Create a spy of the actual MessageConsumer instance
        MessageConsumer messageConsumer = spy(new MessageConsumer());

        // Mock the PriorityBlockingQueue
        PriorityBlockingQueue<MessageDto> messageQueueMock = mock(PriorityBlockingQueue.class);
        doReturn(messageQueueMock).when(messageConsumer).getMessageQueue();

        // Initialize the messageConsumer
        messageConsumer.init();

        // Simulate sending mock messages with varying priorities
        MessageDto message1 = new MessageDto("1", "content1", 1693333777893L, 3);
        MessageDto message2 = new MessageDto("2", "content2", 1693333777893L, 2);
        MessageDto message3 = new MessageDto("3", "content3", 1693333777893L, 4);


        // Simulate calling handleMessage for the actual instance
        messageConsumer.handleMessage(message1);
        messageConsumer.handleMessage(message2);
        messageConsumer.handleMessage(message3);
//        messageConsumer.handleMessage(message4);
//        messageConsumer.handleMessage(message5);
//        messageConsumer.handleMessage(message6);

        // Wait for processing to complete
        TimeUnit.SECONDS.sleep(5);

        // Verify the interactions
        verify(messageConsumer, times(1)).init();
        verify(messageConsumer, times(1)).startConsumingMessages();

        // Verify the order of processed messages
//        InOrder inOrder = inOrder(messageQueueMock);
//        inOrder.verify(messageQueueMock).add(message1);
//        inOrder.verify(messageQueueMock).add(message2);
//        inOrder.verify(messageQueueMock).add(message3);

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
