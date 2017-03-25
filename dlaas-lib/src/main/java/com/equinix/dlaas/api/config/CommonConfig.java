package com.equinix.dlaas.api.config;

import com.equinix.dataflix.SpringLogger;
import com.equinix.dlaas.api.domain.SimpleMessage;
import com.equinix.dlaas.api.domain.SimpleRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.support.collections.DefaultRedisList;
import org.springframework.data.redis.support.collections.DefaultRedisMap;
import org.springframework.data.redis.support.collections.RedisList;
import org.springframework.data.redis.support.collections.RedisMap;
import org.springframework.scheduling.annotation.EnableAsync;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ransay on 1/19/2017.
 */

@Configuration
@EnableSwagger2
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
    public SpringLogger springLogger() {
        SpringLogger springLogger = new SpringLogger();

        Map<String, String> applicationData = new HashMap<>();
        applicationData.put("appName", "JMS_BRIDGE");
        applicationData.put("moduleName", "AUTO_GENERATE");
        springLogger.setApplicationData(applicationData);

        Map<String, String> requestData = new HashMap<>();
        applicationData.put("correlationId", "AUTO_GENERATE");
        springLogger.setApplicationData(requestData);

        return springLogger;
    }

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
