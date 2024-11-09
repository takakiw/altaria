package com.altaria.redis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.database:0}")
    private int redisDatabase;


    @Bean
    @SuppressWarnings("all")
    public RedisTemplate<String, Object> redisTemplate() {
        // 创建一个 RedisTemplate 实例
        RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();

        LettuceConnectionFactory factory = new LettuceConnectionFactory();
        factory.setDatabase(redisDatabase);
        factory.setHostName(redisHost);
        factory.setPort(redisPort);
        factory.setValidateConnection(true);  // 启用连接验证
        factory.afterPropertiesSet();
        template.setConnectionFactory(factory);

        // 创建 StringRedisSerializer 实例，用于序列化和反序列化 Redis 的 key
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // 设置 RedisTemplate 的 key 序列化器
        template.setKeySerializer(stringRedisSerializer);

        // 设置 RedisTemplate 的 hash key 序列化器
        template.setHashKeySerializer(stringRedisSerializer);

        // 设置 RedisTemplate 的 value 序列化器为 jackson2JsonRedisSerializer
        template.setValueSerializer(getJackson2JsonRedisSerializer());

        // 设置 RedisTemplate 的 hash value 序列化器为 jackson2JsonRedisSerializer
        template.setHashValueSerializer(getJackson2JsonRedisSerializer());

        // 执行一些必要的属性设置
        template.afterPropertiesSet();

        // 返回配置好的 RedisTemplate 实例
        return template;
    }

    @Bean
    public Jackson2JsonRedisSerializer getJackson2JsonRedisSerializer() {
        // 创建 Jackson2JsonRedisSerializer 实例
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);

        // 创建 ObjectMapper 实例
        ObjectMapper om = new ObjectMapper();

        // 设置 ObjectMapper 的可见性配置，使其能够访问所有字段
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        // 开启默认的类型序列化功能，非最终类也会有类型信息
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        // 配置 JavaTimeModule 以支持 LocalDate 和 LocalDateTime 的序列化和反序列化
        JavaTimeModule timeModule = new JavaTimeModule();
        timeModule.addDeserializer(LocalDate.class,
                new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        timeModule.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        timeModule.addSerializer(LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        timeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 禁用将日期写为时间戳的功能
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 注册 JavaTimeModule 到 ObjectMapper
        om.registerModule(timeModule);

        // 将配置好的 ObjectMapper 设置到 Jackson2JsonRedisSerializer 中
        jackson2JsonRedisSerializer.setObjectMapper(om);

        return jackson2JsonRedisSerializer;
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        return scheduler;
    }

    @Bean
    public CheckConnection checkConnectTask() {
        return new CheckConnection();
    }
}