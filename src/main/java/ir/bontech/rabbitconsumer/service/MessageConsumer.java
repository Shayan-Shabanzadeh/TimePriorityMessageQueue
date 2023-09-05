package ir.bontech.rabbitconsumer.service;

import ir.bontech.rabbitconsumer.dto.MessageDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

@Service
@Getter
@Primary
@NoArgsConstructor
public class MessageConsumer {

    @Value("${consumer.thread.pool.size:1}")
    private int threadPoolSize;
    protected final PriorityBlockingQueue<MessageDto> messageQueue = new PriorityBlockingQueue<>();
    private ExecutorService executorService;

    private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);


    public MessageConsumer(int threadPoolSize){
        this.threadPoolSize = threadPoolSize;
    }


    //Method to start startConsumingMessage to process messages
    @PostConstruct
    public void init() {
        logger.info("Consumer thread pool started with {} threads.", threadPoolSize);
        executorService = Executors.newFixedThreadPool(threadPoolSize); // Create ExecutorService after properties are read
        startConsumingMessages();
    }

    //Method to consume message from Rabbit
    public void handleMessage(MessageDto message) {
        logger.info("Consume message: {}", message);

        messageQueue.add(message);
    }


    // Method to start consuming message from Priority queue and process them
    public void startConsumingMessages() {
        executorService.execute(() -> {
            while (true) {
                try {
                    List<MessageDto> messages = new ArrayList<>();
                    MessageDto message = messageQueue.poll();
                    if (message != null) {
                        messages.add(message);
                        messageQueue.drainTo(messages, 199); // Try to add up to 4 more messages
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



    //Method for processing messages
    protected void processMessages(List<MessageDto> messages) throws InterruptedException {
        // Implement your batch message processing logic here
        for (MessageDto message : messages) {
            logger.info("Processing message: {}", message);
        }
        logger.info("-------------------------------");
    }

    // Method to gracefully shut down the consumer
    public void stopConsumingMessages() {
        executorService.shutdownNow();
    }
}


