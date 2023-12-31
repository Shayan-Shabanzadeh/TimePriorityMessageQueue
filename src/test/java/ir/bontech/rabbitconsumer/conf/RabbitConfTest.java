package ir.bontech.rabbitconsumer.conf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;


@SpringBootTest
@ActiveProfiles("test")
public class RabbitConfTest {

    @Autowired
    private Queue myQueue;

    @Autowired
    private RabbitConf rabbitConf;

    @Autowired
    private MessageConverter messageConverter;


    @Container
    static RabbitMQContainer rabbitContainer;

    @Value("${spring.rabbitmq.template.exchange}")
    private String exchangeName;

    @Value("${spring.rabbitmq.template.default-receive-queue}")
    private String queueName;


    @BeforeAll
    static void setUpContainer() {
        rabbitContainer = new RabbitMQContainer("rabbitmq:3.8-management-alpine")
                .withReuse(true);
        rabbitContainer.start(); // Start the RabbitMQ container

        // Dynamically get the container's host and port
        String rabbitHost = rabbitContainer.getHost();
        int rabbitPort = rabbitContainer.getMappedPort(5672); // RabbitMQ default port

        // Set the properties for Spring RabbitMQ to use the container's host and port
        System.setProperty("spring.rabbitmq.host", rabbitHost);
        System.setProperty("spring.rabbitmq.port", String.valueOf(rabbitPort));
    }


    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this); // Initialize the mock annotations
    }

    @Test
    public void testMyQueueProperties() {
        // Test properties of the myQueue bean
        Assertions.assertNotNull(myQueue);
        Assertions.assertEquals(queueName, myQueue.getName());
    }

    @Test
    public void testRabbitTemplateProperties() {
        Assertions.assertNotNull(rabbitConf.rabbitTemplate(null, messageConverter));
    }

    @Test
    public void testTopicExchangeProperties() {
        Assertions.assertNotNull(rabbitConf.topicExchange());
        Assertions.assertEquals(exchangeName, rabbitConf.topicExchange().getName());
    }

    @Test
    public void testQueueBinding() {
        Assertions.assertNotNull(rabbitConf.queueBinding());
    }

    @Test
    public void testJsonMessageConverter() {
        Assertions.assertNotNull(rabbitConf.jsonMessageConverter());
    }

    @Test
    public void testRetryTemplate() {
        Assertions.assertNotNull(rabbitConf.retryTemplate());
    }

    @Test
    public void testSimpleRetryPolicy() {
        Assertions.assertNotNull(rabbitConf.simpleRetryPolicy());
    }

    @Test
    public void testExponentialBackOffPolicy() {
        Assertions.assertNotNull(rabbitConf.exponentialBackOffPolicy());
    }

}
