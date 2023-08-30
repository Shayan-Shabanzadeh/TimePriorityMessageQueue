package ir.bontech.rabbitconsumer.controller;

import ir.bontech.rabbitconsumer.dto.MessageDto;
import ir.bontech.rabbitconsumer.service.MessagePublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MessageControllerTest {

    @Mock
    private MessagePublisher messagePublisher;

    private MessageController messageController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        messageController = new MessageController(messagePublisher);
    }

    @Test
    void testPublishMessageSuccess() {
        MessageDto message = MessageDto.builder()
                .id("1")
                .content("test")
                .creationTimestamp(1000000543000L)
                .priority(5)
                .build();

        ResponseEntity<String> response = messageController.publishMessage(message);

        verify(messagePublisher, times(1)).sendMessage(message);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Message published successfully", response.getBody());
    }

    @Test
    void testPublishMessageFailure() {
        MessageDto message = MessageDto.builder()
                .id("1")
                .content("test")
                .creationTimestamp(1000000543000L)
                .priority(5)
                .build();

        doThrow(new RuntimeException("Some error")).when(messagePublisher).sendMessage(message);

        ResponseEntity<String> response = messageController.publishMessage(message);

        verify(messagePublisher, times(1)).sendMessage(message);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error publishing message: Some error", response.getBody());
    }
}
