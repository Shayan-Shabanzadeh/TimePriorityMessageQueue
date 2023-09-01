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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RabbitConf {

    @Value("${spring.rabbitmq.host}")
    private String rabbitHost;

    @Value("${spring.rabbitmq.port}")
    private int rabbitPort;
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


    @Bean
    @Primary
    public ConnectionFactory cachingConnectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setHost(rabbitHost);
        cachingConnectionFactory.setPort(rabbitPort);
        cachingConnectionFactory.setUsername(rabbitUsername);
        cachingConnectionFactory.setPassword(rabbitPassword);
        cachingConnectionFactory.setConnectionNameStrategy(connectionFactory -> "connection-name");
        return cachingConnectionFactory;
    }


    @Bean
    public TopicExchange topicExchange() {
        return ExchangeBuilder.topicExchange(exchangeName)
                .durable(true)
                .build();
    }


    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setExchange(exchangeName);
        rabbitTemplate.setRoutingKey(routingKey);
        rabbitTemplate.setRetryTemplate(retryTemplate());
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                // Handle successful message confirmation
                System.out.println("failed to send message");
            } else {
                // Handle failed message confirmation
                System.out.println("message send successfully");
            }
        });
        return rabbitTemplate;
    }

    @Bean
    public Queue myQueue() {
        return QueueBuilder.durable(queueName)
                .withArgument("x-queue-type", queueType)
                .build();
    }

    @Bean
    public Binding queueBinding() {
        return BindingBuilder.bind(myQueue())
                .to(topicExchange())
                .with(routingKey); // You can use wildcards in the routing key pattern
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(simpleRetryPolicy());
        retryTemplate.setBackOffPolicy(exponentialBackOffPolicy());
        return retryTemplate;
    }


    @Bean
    public RetryPolicy simpleRetryPolicy() {
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3); // Maximum number of retry attempts
        return retryPolicy;
    }

    @Bean
    public ExponentialBackOffPolicy exponentialBackOffPolicy() {
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000); // Initial interval in milliseconds
        backOffPolicy.setMultiplier(2.0); // Backoff multiplier
        backOffPolicy.setMaxInterval(10000); // Maximum interval in milliseconds
        return backOffPolicy;
    }



    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public MessageListenerAdapter channelAdapter(MessageConsumer messageConsumer, MessageConverter messageConverter){
        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(messageConsumer, "handleMessage");
        messageListenerAdapter.setMessageConverter(messageConverter);

        return messageListenerAdapter;
    }


    @Bean
    public RetryOperationsInterceptor retryBackoff(){
        return RetryInterceptorBuilder.stateless()
                .backOffOptions(100,3.0,1000) // 100ms wait time (delay) to retry
                .maxAttempts(3)
                .recoverer(new RejectAndDontRequeueRecoverer()) // Callback for message that was consumed but failed all retry attempts.
                .build();
    }

    @Bean
    public SimpleMessageListenerContainer createMessageListenerContainer(ConnectionFactory connectionFactory
            , @Value("${spring.rabbitmq.template.default-receive-queue}") String queueName
            , MessageListenerAdapter messageListenerAdapter
            , RetryOperationsInterceptor retryOperationsInterceptor){
        SimpleMessageListenerContainer messageListenerContainer = new SimpleMessageListenerContainer(connectionFactory);
        messageListenerContainer.addQueueNames(queueName);
        messageListenerContainer.setMessageListener(messageListenerAdapter);
        messageListenerContainer.setAdviceChain(retryOperationsInterceptor);
        return messageListenerContainer;
    }


}
