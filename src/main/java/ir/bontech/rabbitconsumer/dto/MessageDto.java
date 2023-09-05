package ir.bontech.rabbitconsumer.dto;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Component
public class MessageDto implements Comparable<MessageDto> {


    private static long timeWindowSeconds;

    @Value("${messageDto.priorityIncreaseTimeWindowSeconds:60}")
    public static void setTimeWindowSeconds(long timeWindowSeconds) {
        MessageDto.timeWindowSeconds = timeWindowSeconds;
    }

    private String id;
    private String content;
    private long creationTimestamp;
    private int priority;


    @Override
    public String toString() {
        Instant instant = Instant.ofEpochMilli(creationTimestamp);
        long currentTime = Instant.now().toEpochMilli();
        LocalDateTime creationTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return "MessageDto{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", creationTimestamp=" + formatter.format(creationTime) +
                ", priority=" + priority +
                ", calculated priority=" + calculateCompositeScore(this, currentTime) +
                '}';
    }


    @Override
    public int compareTo(@NonNull MessageDto other) {
        Instant instant = Instant.now();
        long timeStampMillis = instant.toEpochMilli();

        // Calculate composite scores
        double thisCompositeScore = calculateCompositeScore(this, timeStampMillis);
        double otherCompositeScore = calculateCompositeScore(other, timeStampMillis);

        // Compare based on composite scores
        int isBiggerByPriority = Double.compare(otherCompositeScore, thisCompositeScore); // Higher score first
        if (isBiggerByPriority == 0) {
            int isBiggerByCreationTime = Double.compare(this.creationTimestamp, other.creationTimestamp);
            if (isBiggerByCreationTime == 0) {
                return Integer.compare(this.priority, other.priority);
            }
            return isBiggerByCreationTime;
        }
        return isBiggerByPriority;
    }

    private int calculateCompositeScore(MessageDto message, long currentTime) {
        var timeWindowMillis = timeWindowSeconds * 1000;
        int additional_priority = (int) ((currentTime - message.creationTimestamp) / timeWindowMillis);
        return additional_priority + message.getPriority();
    }

}