package ir.bontech.rabbitconsumer.controller;

import ir.bontech.rabbitconsumer.dto.MessageDto;
import ir.bontech.rabbitconsumer.service.MessagePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    private final MessagePublisher messagePublisher;

    @Autowired
    public MessageController(MessagePublisher messagePublisher) {
        this.messagePublisher = messagePublisher;
    }

    @PostMapping(value = "/publish", consumes = "application/json")
    public ResponseEntity<String> publishMessage(@RequestBody MessageDto message) {
        try {
            messagePublisher.sendMessage(message);
            return ResponseEntity.ok("Message published successfully");
        } catch (Exception e) {
            // Handle unexpected exceptions
            String errorMessage = "An unexpected error occurred while publishing the message.";
            // Log the error
            logger.error(errorMessage, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }
}


