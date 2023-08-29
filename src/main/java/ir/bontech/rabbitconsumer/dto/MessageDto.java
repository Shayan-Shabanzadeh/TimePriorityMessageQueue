package ir.bontech.rabbitconsumer.dto;

import lombok.*;

import java.time.Instant;

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
    private static final long TIME_WINDOW = 2 * 60 * 1000; // 2 minutes in milliseconds

    @Override
    public int compareTo(MessageDto other) {
        // Calculate composite scores
        Instant instant = Instant.now();
        long timeStampMillis = instant.toEpochMilli();
        double timeElapsed = timeStampMillis - this.creationTimestamp;

        // Adjust timestamp for messages created within the last 2 minutes
        if (timeElapsed < TIME_WINDOW) {
            double timeAdjustment = (TIME_WINDOW - timeElapsed) / TIME_WINDOW;
            timeElapsed += timeAdjustment;
        }

        double thisCompositeScore = (PRIORITY_WEIGHT * this.priority) + (TIMESTAMP_WEIGHT * (timeElapsed - calculatePriorityTimeAdjustment(this)));
        double otherCompositeScore = (PRIORITY_WEIGHT * other.priority) + (TIMESTAMP_WEIGHT * ((timeElapsed - (other.creationTimestamp - this.creationTimestamp)) - calculatePriorityTimeAdjustment(other)));

        // Compare based on composite scores
        return Double.compare(otherCompositeScore, thisCompositeScore); // Higher score first
    }

    private double calculatePriorityTimeAdjustment(MessageDto message) {
        double timeElapsed = System.currentTimeMillis() - message.creationTimestamp;

        if (timeElapsed >= TIME_WINDOW) {
            return 0;
        } else {
            double timeAdjustment = ((double) (TIME_WINDOW - timeElapsed)) / TIME_WINDOW;
            return timeAdjustment * ((message.priority - 1) * 100); // Adjust based on priority difference
        }
    }
}
