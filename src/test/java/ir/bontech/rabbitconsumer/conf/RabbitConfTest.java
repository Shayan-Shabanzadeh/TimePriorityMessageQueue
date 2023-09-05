package ir.bontech.rabbitconsumer.conf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;


@SpringBootTest
@ActiveProfiles("test")
public class RabbitConfTest {

    @Autowired
    private Queue myQueue;

    @Autowired
    private RabbitConf rabbitConf;


    @Mock
    private RabbitTemplate rabbitTemplate;


    @Container
    static RabbitMQContainer rabbitContainer = new RabbitMQContainer("rabbitmq:3.8-management-alpine")
            .withReuse(true);

    @BeforeAll
    static void setUpContainer() {
        rabbitContainer.start(); // Start the RabbitMQ container
    }

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitContainer::getAmqpPort);
    }


    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this); // Initialize the mock annotations
    }


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

    @Test
    public void testMyQueueProperties() {
        // Test properties of the myQueue bean
        Assertions.assertNotNull(myQueue);
        Assertions.assertEquals(queueName, myQueue.getName());
    }

    @Test
    public void testRabbitTemplateProperties() {
        Assertions.assertNotNull(rabbitConf.rabbitTemplate(null));
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
