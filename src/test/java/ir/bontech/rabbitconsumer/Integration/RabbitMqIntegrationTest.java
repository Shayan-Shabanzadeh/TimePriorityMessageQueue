package ir.bontech.rabbitconsumer.Integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.bontech.rabbitconsumer.MessageGenerator;
import ir.bontech.rabbitconsumer.conf.RabbitConf;
import ir.bontech.rabbitconsumer.dto.MessageDto;
import ir.bontech.rabbitconsumer.service.MessageConsumer;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
@SpringJUnitConfig(classes = {RabbitConf.class , MessageConsumer.class}) // Import your RabbitMQ configuration
public class RabbitMqIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.host}")
    private String rabbitMqHost;

    @Value("${spring.rabbitmq.port}")
    private Integer rabbitMqPort;

    @Value("${spring.rabbitmq.username}")
    private String rabbitUsername;

    @Value("${spring.rabbitmq.password}")
    private String rabbitPassword;

    @Value("${spring.rabbitmq.template.exchange}")
    private String exchangeName;

    @Value("${spring.rabbitmq.template.default-receive-queue}")
    private String queueName;

    @Value("${spring.rabbitmq.queue.type}")
    private String queueType;


    @Value("${spring.rabbitmq.template.routing-key}")
    private String routingKey;

    @Value("${spring.rabbitmq.listener.simple.auto-startup}")
    private boolean autoStartup;

    @Value("${spring.rabbitmq.listener.direct.consumers-per-queue}")
    private int consumersPerQueue;

    @Value("${spring.rabbitmq.listener.simple.acknowledge-mode}")
    private String acknowledgeMode;

    @Value("${spring.rabbitmq.listener.simple.prefetch}")
    private int prefetchCount;

    @Value("${spring.rabbitmq.listener.simple.retry.enabled}")
    private boolean retryEnabled;

    @Value("${spring.rabbitmq.listener.simple.retry.initial-interval}")
    private long retryInitialInterval;

    @Value("${spring.rabbitmq.listener.simple.retry.max-attempts}")
    private int retryMaxAttempts;


    final ObjectMapper objectMapper = new ObjectMapper();

    @Container
    static RabbitMQContainer rabbitContainer = new RabbitMQContainer("rabbitmq:3.8-management-alpine")
            .withReuse(true);


    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitContainer::getAmqpPort);
    }
    @Test
    public void testRabbitMQComponents() {
        assertTrue(rabbitTemplate.getConnectionFactory().getVirtualHost().startsWith("/"));
        assertNotNull(rabbitTemplate.getExchange());
        assertNotNull(rabbitTemplate.getRoutingKey());
    }


    @Test
    public void testQueueExists() {
        // You can use RabbitTemplate to check if the queue exists
        boolean queueExists = Boolean.TRUE.equals(rabbitTemplate.execute(channel -> {
            try {
                channel.queueDeclarePassive(queueName); // Throws an exception if the queue doesn't exist
                return true;
            } catch (IOException e) {
                return false;
            }
        }));

        assertTrue(queueExists);
    }


    @Test
    public void testSendMessagesFromJsonFileToRabbitMQ() throws IOException {
        MessageGenerator.createAndWriteMessagesToJsonFile("messages.json");
        List<MessageDto> messages = readMessagesFromJsonFile("./messages.json");

        for (MessageDto message : messages) {
            rabbitTemplate.convertAndSend(exchangeName, "", message);
        }

    }


    private List<MessageDto> readMessagesFromJsonFile(String fileName) throws IOException {
        File file = new File(fileName);
        return Arrays.asList(objectMapper.readValue(file, MessageDto[].class));
    }

}







