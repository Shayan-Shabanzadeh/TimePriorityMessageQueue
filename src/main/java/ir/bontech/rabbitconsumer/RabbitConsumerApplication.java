package ir.bontech.rabbitconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class RabbitConsumerApplication {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(RabbitConsumerApplication.class, args);
	}

}
