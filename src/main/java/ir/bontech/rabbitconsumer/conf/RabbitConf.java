package ir.bontech.rabbitconsumer.conf;

import ir.bontech.rabbitconsumer.service.MessageConsumer;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@EnableConfigurationProperties(RabbitProperties.class)
public class RabbitConf {

    // Exchange properties
    @Value("${spring.rabbitmq.template.exchange}")
    private String exchangeName;

    // Queue properties
    @Value("${spring.rabbitmq.template.default-receive-queue}")
    private String queueName;

    @Value("${spring.rabbitmq.queue.type}")
    private String queueType;

    // Routing key
    @Value("${spring.rabbitmq.template.routing-key}")
    private String routingKey;

    // Listener properties
    @Value("${spring.rabbitmq.listener.simple.auto-startup}")
    private boolean autoStartup;

    @Value("${spring.rabbitmq.listener.direct.consumers-per-queue}")
    private int consumersPerQueue;

    @Value("${spring.rabbitmq.listener.simple.acknowledge-mode}")
    private String acknowledgeMode;

    @Value("${spring.rabbitmq.listener.simple.prefetch}")
    private int prefetchCount;


    @Value("${spring.rabbitmq.listener.simple.retry.initial-interval}")
    private long retryInitialInterval;

    @Value("${spring.rabbitmq.listener.simple.retry.max-attempts}")
    private int retryMaxAttempts;

    private final RabbitProperties rabbitProperties;


    @Autowired
    public RabbitConf(RabbitProperties rabbitProperties) {
        this.rabbitProperties = rabbitProperties;
    }


    // Define a ConnectionFactory bean for RabbitMQ connection
    @Bean
    @Primary
    public ConnectionFactory cachingConnectionFactory() {
        // Configuration for the RabbitMQ connection
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setHost(rabbitProperties.getHost());
        cachingConnectionFactory.setPort(rabbitProperties.getPort());
        cachingConnectionFactory.setUsername(rabbitProperties.getUsername());
        cachingConnectionFactory.setPassword(rabbitProperties.getPassword());
        cachingConnectionFactory.setConnectionNameStrategy(connectionFactory -> "connection-name");
        return cachingConnectionFactory;
    }


    // Define a RabbitTemplate bean
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        // Configuration for the RabbitTemplate used to send messages
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setExchange(exchangeName);
        rabbitTemplate.setRoutingKey(routingKey);
        rabbitTemplate.setMessageConverter(messageConverter);
        rabbitTemplate.setRetryTemplate(retryTemplate());
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            // Callback for message confirmation
            if (ack) {
                // Handle successful message confirmation
                System.out.println("Message send successfully");
            } else {
                // Handle failed message confirmation
                System.out.println("Failed to send message");
            }
        });
        return rabbitTemplate;
    }

    // Define a TopicExchange bean
    @Bean
    public TopicExchange topicExchange() {
        // Configuration for a TopicExchange
        return ExchangeBuilder.topicExchange(exchangeName)
                .durable(true)
                .build();
    }


    // Define a Queue bean
    @Bean
    public Queue myQueue() {
        // Configuration for a RabbitMQ queue
        return QueueBuilder.durable(queueName)
                .withArgument("x-queue-type", queueType)
                .build();
    }

    // Define a Binding bean
    @Bean
    public Binding queueBinding() {
        // Configuration for binding the queue to the exchange
        return BindingBuilder.bind(myQueue())
                .to(topicExchange())
                .with(routingKey); // You can use wildcards in the routing key pattern
    }

    // Define a MessageConverter bean for JSON messages
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Define a RetryTemplate bean for message retry
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(simpleRetryPolicy());
        retryTemplate.setBackOffPolicy(exponentialBackOffPolicy());
        return retryTemplate;
    }

    // Define a RetryPolicy bean for simple retry logic
    @Bean
    public RetryPolicy simpleRetryPolicy() {
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3); // Maximum number of retry attempts
        return retryPolicy;
    }

    // Define an ExponentialBackOffPolicy bean for backoff intervals
    @Bean
    public ExponentialBackOffPolicy exponentialBackOffPolicy() {
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000); // Initial interval in milliseconds
        backOffPolicy.setMultiplier(2.0); // Backoff multiplier
        backOffPolicy.setMaxInterval(10000); // Maximum interval in milliseconds
        return backOffPolicy;
    }


    // Define RabbitAdmin bean
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    // Define MessageListenerAdapter bean
    @Bean
    public MessageListenerAdapter messageListenerAdapter(MessageConsumer messageConsumer, MessageConverter messageConverter) {
        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(messageConsumer, "handleMessage");
        messageListenerAdapter.setMessageConverter(messageConverter);
        return messageListenerAdapter;
    }

    // Define a RetryOperationsInterceptor bean for message retry with advice chain
    @Bean
    public RetryOperationsInterceptor retryBackoff() {
        //TODO  Configure  retry interceptor here
        return RetryInterceptorBuilder.stateless()
                .backOffOptions(100, 3.0, 1000) // 100ms wait time (delay) to retry
                .maxAttempts(3)
                .recoverer(new RejectAndDontRequeueRecoverer()) // Callback for message that was consumed but failed all retry attempts.
                .build();
    }


    // Define a SimpleMessageListenerContainer bean for consuming messages
    @Bean
    public SimpleMessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory
            , @Value("${spring.rabbitmq.template.default-receive-queue}") String queueName
            , MessageListenerAdapter messageListenerAdapter
            , RetryOperationsInterceptor retryOperationsInterceptor) {
        SimpleMessageListenerContainer messageListenerContainer = new SimpleMessageListenerContainer(connectionFactory);
        messageListenerContainer.addQueueNames(queueName);
        messageListenerContainer.setMessageListener(messageListenerAdapter);
        messageListenerContainer.setAdviceChain(retryOperationsInterceptor);
        messageListenerContainer.setAcknowledgeMode(AcknowledgeMode.valueOf(acknowledgeMode));
        messageListenerContainer.setAutoStartup(autoStartup);
        messageListenerContainer.setConcurrentConsumers(consumersPerQueue);
        messageListenerContainer.setPrefetchCount(prefetchCount);
        messageListenerContainer.setDeclarationRetries(retryMaxAttempts);
        messageListenerContainer.setRetryDeclarationInterval(retryInitialInterval);
        return messageListenerContainer;
    }

}

