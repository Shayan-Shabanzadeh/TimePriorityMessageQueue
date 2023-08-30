package ir.bontech.rabbitconsumer.service;

import ir.bontech.rabbitconsumer.dto.MessageDto;
import lombok.Getter;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
@Getter
public class MessageConsumer {

    @Value("${spring.rabbitmq.template.default-receive-queue}")
    private String queueName;

    private final PriorityBlockingQueue<MessageDto> messageQueue = new PriorityBlockingQueue<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(1); // Number of consumer threads

    @PostConstruct
    public void init() {
        startConsumingMessages();
    }

    @RabbitListener(queues = "${spring.rabbitmq.template.default-receive-queue}")
    public void handleMessage(MessageDto message) {
        // Assuming you want to prioritize based on the 'priority' field of MessageDto
        System.out.println("Consume message: " + message);
        messageQueue.add(message);
    }




    // Method to start consuming and processing messages
    public void startConsumingMessages() {
        executorService.execute(() -> {
            while (true) {
                try {
                    List<MessageDto> messages = new ArrayList<>();
                    MessageDto message = messageQueue.poll(); // Non-blocking operation
                    if (message != null) {
                        messages.add(message);
                        messageQueue.drainTo(messages, 4); // Try to add up to 4 more messages
                        processMessages(messages);
                    } else {
                        Thread.sleep(1000); // Sleep for a while if there are no messages
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    void processMessages(List<MessageDto> messages) {
        // Implement your batch message processing logic here
        for (MessageDto message : messages) {
            System.out.println("Processing message: " + message);
        }
        System.out.println("-------------------------------");
    }

    // Method to gracefully shut down the consumer
    public void stopConsumingMessages() {
        executorService.shutdownNow();
    }
}


