package ir.bontech.rabbitconsumer.dto;

import lombok.*;



import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto implements Comparable<MessageDto> {
    private String id;
    private String content;
    private long creationTimestamp;
    private int priority;

    // Weights for priority and timestamp
    private static final double PRIORITY_WEIGHT = 0.7;
    private static final double TIMESTAMP_WEIGHT = 0.3;

    @Override
    public int compareTo(MessageDto other) {
        // Calculate composite scores
        Instant instant = Instant.now();
        long timeStampMillis = instant.toEpochMilli();
        System.out.println("current time : "+ timeStampMillis);
        double thisCompositeScore = (PRIORITY_WEIGHT * this.priority) + (TIMESTAMP_WEIGHT * (timeStampMillis -  this.creationTimestamp));
        double otherCompositeScore = (PRIORITY_WEIGHT * other.priority) + (TIMESTAMP_WEIGHT * (timeStampMillis -   other.creationTimestamp));

        // Compare based on composite scores
        return Double.compare(otherCompositeScore, thisCompositeScore); // Higher score first
    }
}

