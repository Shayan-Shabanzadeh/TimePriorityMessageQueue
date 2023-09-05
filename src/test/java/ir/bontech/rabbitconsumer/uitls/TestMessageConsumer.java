package ir.bontech.rabbitconsumer.uitls;

import ir.bontech.rabbitconsumer.dto.MessageDto;
import ir.bontech.rabbitconsumer.service.MessageConsumer;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Getter
public class TestMessageConsumer extends MessageConsumer {

    private final Map<Integer, List<Long>> processingTimeMap = new HashMap<>();
    private final Map<Integer, Long> maxProcessingTimeMap = new HashMap<>();
    private final Map<Integer, List<Long>> queueTimeMap = new HashMap<>();



    @Override
    public void handleMessage(MessageDto message) {
        long queueTime = System.currentTimeMillis(); // Record the time when the message enters the queue
        System.out.println("Consume message: " + message);
        messageQueue.add(message);
        recordQueueTime(message.getPriority(), queueTime);
    }

    private void recordQueueTime(int priority, long queueTime) {
        queueTimeMap.computeIfAbsent(priority, k -> new ArrayList<>()).add(queueTime);
    }

    @Override
    protected void processMessages(List<MessageDto> messages) {
        // Implement your batch message processing logic here
        for (MessageDto message : messages) {
            System.out.println("Processing message: " + message);
            long queueTime = getQueueTime(message.getPriority());
            long processTime = System.currentTimeMillis() - queueTime;
            recordProcessingTime(message.getPriority(), processTime);
            simulateProcessingTime(10);
        }
        System.out.println("-------------------------------");
        calculateStats();
        System.out.println("-------------------------------");
    }


    private void simulateProcessingTime(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private long getQueueTime(int priority) {
        List<Long> queueTimes = queueTimeMap.get(priority);
        if (queueTimes != null && !queueTimes.isEmpty()) {
            return queueTimes.get(queueTimes.size() - 1); // Get the time when the last message of the specified priority entered the queue
        }
        return 0; // Return 0 if no queue times are recorded for the specified priority
    }

    private void recordProcessingTime(int priority, long processTime) {
        processingTimeMap.computeIfAbsent(priority, k -> new ArrayList<>()).add(processTime);
        long maxProcessingTime = maxProcessingTimeMap.getOrDefault(priority, 0L);
        if (processTime > maxProcessingTime) {
            maxProcessingTimeMap.put(priority, processTime);
        }
    }

    public void calculateStats() {
        for (Map.Entry<Integer, List<Long>> entry : processingTimeMap.entrySet()) {
            int priority = entry.getKey();
            List<Long> processingTimes = entry.getValue();

            if (!processingTimes.isEmpty()) {
                long sum = processingTimes.stream().mapToLong(Long::longValue).sum();
                long max = maxProcessingTimeMap.getOrDefault(priority, 0L);
                long min = Collections.min(processingTimes);
                double average = (double) sum / processingTimes.size();

                String maxTime = formatTime(max);
                String minTime = formatTime(min);
                String averageTime = formatTime((long) average);

                System.out.println("Priority " + priority + ":");
                System.out.println("   Max Processing Time: " + maxTime);
                System.out.println("   Min Processing Time: " + minTime);
                System.out.println("   Average Processing Time: " + averageTime);
            }
        }
    }

    private String formatTime(long timeInMillis) {
        if (timeInMillis >= 1000) {
            double seconds = (double) timeInMillis / 1000;
            return String.format("%.2f seconds", seconds);
        } else {
            return timeInMillis + " milliseconds";
        }
    }
}
