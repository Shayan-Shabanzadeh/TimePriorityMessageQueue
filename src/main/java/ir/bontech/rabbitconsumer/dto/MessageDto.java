package ir.bontech.rabbitconsumer.dto;

import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageDto implements Comparable<MessageDto> {
    private String id;
    private String content;
    private long creationTimestamp;
    private int priority;

    // Weights for priority and timestamp
//    private static final double PRIORITY_WEIGHT = 0.7;
//    private static final double TIMESTAMP_WEIGHT = 0.3;
    private static final long TIME_WINDOW = 2 * 60 * 1000; // 2 minutes in milliseconds


    @Override
    public String toString() {
        Instant instant = Instant.ofEpochMilli(creationTimestamp);
        LocalDateTime creationTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return "MessageDto{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", creationTimestamp=" + formatter.format(creationTime) +
                ", priority=" + priority +
                '}';
    }


    @Override
    public int compareTo(MessageDto other) {
        // Calculate composite scores
        Instant instant = Instant.now();
        long timeStampMillis = instant.toEpochMilli();


        double thisCompositeScore = calculateCompositeScore(this, timeStampMillis);
        double otherCompositeScore = calculateCompositeScore(other, timeStampMillis);

        // Compare based on composite scores
        int isBiggerByPriority =  Double.compare(otherCompositeScore, thisCompositeScore); // Higher score first
        if (isBiggerByPriority == 0 ) {
            int isBiggerByCreationTime = Double.compare(this.creationTimestamp, other.creationTimestamp);
            if (isBiggerByCreationTime == 0 ){
                return Integer.compare(this.priority ,other.priority);
            }
            return isBiggerByCreationTime;
        }
        return isBiggerByPriority;
    }

    private double calculateCompositeScore(MessageDto message, long currentTime) {
        int additional_priority = (int) ((currentTime - message.creationTimestamp) / TIME_WINDOW);
        return additional_priority + message.getPriority();
    }

}