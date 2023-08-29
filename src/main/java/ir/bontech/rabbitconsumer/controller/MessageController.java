package ir.bontech.rabbitconsumer.controller;

import ir.bontech.rabbitconsumer.dto.MessageDto;
import ir.bontech.rabbitconsumer.service.MessagePublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessagePublisher messagePublisher;

    @Autowired
    public MessageController(MessagePublisher messagePublisher) {
        this.messagePublisher = messagePublisher;
    }

    @PostMapping(value = "/publish", consumes = "application/json")
    public String publishMessage(@RequestBody MessageDto message) {
        try {
            messagePublisher.sendMessage(message);
            return "Message published successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error publishing message: " + e.getMessage();
        }
    }
}

