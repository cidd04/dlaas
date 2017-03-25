package com.equinix.dlaas.api.service;

import com.equinix.dlaas.api.domain.SimpleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.support.collections.RedisList;
import org.springframework.data.redis.support.collections.RedisMap;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Created by ransay on 2/4/2017.
 */
@Service
public class EventReceiverService {

    private static final Logger log = LoggerFactory.getLogger(EventReceiverService.class);

    @Autowired
    private RedisList<SimpleMessage> eventQueue;

    @Autowired
    private RedisMap<String, SimpleMessage> eventMap;

    public void process(SimpleMessage simpleMessage) {

        // 1. Persist in Redis map
        eventMap.put(simpleMessage.getId(), simpleMessage);

        // 2. Push to Redis event queue
        try {
            eventQueue.put(simpleMessage);
        } catch (InterruptedException e) {
            log.info("InterruptedException occured!?!?!");
            throw new RuntimeException("Please Rollback everything!", e);
        }
    }

    @Async
    public void processAsync(SimpleMessage simpleMessage) {
        process(simpleMessage);
    }
}
