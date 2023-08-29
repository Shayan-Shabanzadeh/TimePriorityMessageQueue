package ir.bontech.rabbitconsumer.service;

import ir.bontech.rabbitconsumer.dto.MessageDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.*;

@Service
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
        System.out.println("consume message : " + message);
        messageQueue.add(message);
        System.out.println("queue is : " + messageQueue);
    }

    // Method to start consuming and processing messages
    public void startConsumingMessages() {
        executorService.execute(() -> {
            while (true) {
                try {
                    MessageDto message = messageQueue.take(); // Blocking operation
                    processMessage(message);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    private void processMessage(MessageDto message) throws InterruptedException {
        // Implement your message processing logic here
        System.out.println("Processing message: " + message);
        Thread.sleep(1000);
    }

    // Method to gracefully shut down the consumer
    public void stopConsumingMessages() {
        executorService.shutdownNow();
    }
}
