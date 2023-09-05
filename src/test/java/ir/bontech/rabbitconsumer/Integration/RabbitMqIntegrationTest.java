package ir.bontech.rabbitconsumer.Integration;

import ir.bontech.rabbitconsumer.conf.RabbitConf;
import ir.bontech.rabbitconsumer.dto.MessageDto;
import ir.bontech.rabbitconsumer.uitls.MessageGenerator;
import ir.bontech.rabbitconsumer.uitls.TestMessageConsumer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@SpringJUnitConfig(classes = {RabbitConf.class, TestMessageConsumer.class}) // Import your RabbitMQ configuration
public class RabbitMqIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.template.exchange}")
    private String exchangeName;

    @Value("${spring.rabbitmq.template.default-receive-queue}")
    private String queueName;

    @Autowired
    private RabbitAdmin rabbitAdmin;


    @Container
    static RabbitMQContainer rabbitContainer;

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

    @AfterAll
    static void tearDown() {

        System.out.println(rabbitContainer.getLogs());

        if (rabbitContainer.isRunning()) {
            rabbitContainer.stop();
            rabbitContainer.close();
        }
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
    public void testSendMessagesFromJsonFileToRabbitMQ() throws IOException, InterruptedException {
        var messages = MessageGenerator.generateMessages(1000);
        CountDownLatch latch = new CountDownLatch(1000);

        for (MessageDto message : messages) {
            rabbitTemplate.convertAndSend(exchangeName, "", message);
            latch.countDown();
        }
        latch.await();
        await().atMost(30, SECONDS).until(() -> getMessageCount() > 0); // Change this line

        // Check if there are messages in the queue (greater than 0)
        int messageCount = getMessageCount();
        assertTrue(messageCount > 0, "Messages should be in the queue");

        Thread.sleep(20000);
    }


    private int getMessageCount() {
        return Objects.requireNonNull(rabbitAdmin.getQueueInfo(queueName)).getMessageCount();
    }


}







