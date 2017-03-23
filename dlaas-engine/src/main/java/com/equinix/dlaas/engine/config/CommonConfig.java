package com.equinix.dlaas.engine.config;

import com.equinix.dlaas.engine.domain.SimpleMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.support.collections.DefaultRedisList;
import org.springframework.data.redis.support.collections.DefaultRedisMap;
import org.springframework.data.redis.support.collections.RedisList;
import org.springframework.data.redis.support.collections.RedisMap;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by ransay on 2/10/2017.
 */
@Configuration
@EnableTransactionManagement
@EnableAsync
public class CommonConfig {

    @Value("${topic.event}")
    private String eventTopic;

    @Value("${topic.eventMap}")
    private String masterMapTopic;

    @Value("${redis.notify}")
    private String notifyTopic;

    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private int redisPort;

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory jedisConFactory = new JedisConnectionFactory();
        jedisConFactory.setHostName(redisHost);
        jedisConFactory.setPort(redisPort);
        return jedisConFactory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setEnableTransactionSupport(true);
        template.setDefaultSerializer(new Jackson2JsonRedisSerializer(SimpleMessage.class));
        return template;
    }

    @Bean
    public RedisList<SimpleMessage> eventQueue(RedisTemplate redisTemplate) {
        return new DefaultRedisList<>(
                redisTemplate.boundListOps(eventTopic));
    }

    @Bean
    public RedisList<SimpleMessage> notifyQueue(RedisTemplate redisTemplate) {
        return new DefaultRedisList<>(
                redisTemplate.boundListOps(notifyTopic));
    }

    @Bean
    public RedisMap<String, SimpleMessage> eventMap(RedisTemplate redisTemplate) {
        return new DefaultRedisMap<>(
                redisTemplate.boundHashOps(masterMapTopic));
    }

    @Bean
    public ScheduledExecutorService localExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }

}
