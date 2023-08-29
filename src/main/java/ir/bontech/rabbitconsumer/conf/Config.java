package ir.bontech.rabbitconsumer.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.protobuf.Message;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;

@Configuration
public class Config {

    @Bean
    public ProtobufHttpMessageConverter protobufHttpMessageConverter() {
        return new ProtobufHttpMessageConverter() {
            @Override
            protected boolean supports(Class<?> clazz) {
                // Allow any Protobuf message class
                return Message.class.isAssignableFrom(clazz);
            }
        };
    }

}

