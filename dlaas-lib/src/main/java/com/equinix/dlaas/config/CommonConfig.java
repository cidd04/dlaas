package com.equinix.dlaas.config;

import com.equinix.dlaas.domain.SimpleMessage;
import com.equinix.dlaas.domain.SimpleRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.collections.DefaultRedisList;
import org.springframework.data.redis.support.collections.DefaultRedisMap;
import org.springframework.data.redis.support.collections.RedisList;
import org.springframework.data.redis.support.collections.RedisMap;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Created by ransay on 1/19/2017.
 */

@Configuration
@EnableAspectJAutoProxy
@EnableAsync
public class CommonConfig {

    @Value("${topic.event}")
    private String eventTopic;

    @Value("${topic.notify}")
    private String notifyTopic;

    @Value("${topic.recordMap}")
    private String recordMapTopic;

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
    public RedisMap<String, SimpleRecord> recordMap(RedisTemplate redisTemplate) {
        return new DefaultRedisMap<>(
                redisTemplate.boundHashOps(recordMapTopic));
    }
}
