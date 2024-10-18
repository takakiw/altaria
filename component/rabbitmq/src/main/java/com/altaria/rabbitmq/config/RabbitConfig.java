package com.altaria.rabbitmq.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitConfig {

    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue uploadQueue() {
        return new Queue("upload-queue");
    }

    @Bean
    public Queue deleteAvatarQueue() {
        return new Queue("delete-avatar-queue");
    }

    @Bean
    public Queue deleteFileQueue() {
        return new Queue("delete-file-queue");
    }

    @Bean
    public Queue resultQueue() {
        return new Queue("update-queue");
    }
}