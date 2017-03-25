package com.equinix.dlaas.service;

import com.equinix.dlaas.domain.SimpleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.support.collections.RedisList;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Created by ransay on 2/12/2017.
 */

@Component
public class RedisMessageReceiver {

    private static final Logger log = LoggerFactory.getLogger(RedisMessageReceiver.class);

    @Autowired
    private RedisList<SimpleMessage> extEventQueue;

    @Async
    public void listen(MessageProcessor messageProcessor) {
        // Loop to for continuous listening of queue
        // In order to configure the number of threads that are spawned in process() method,
        // one should create a new Executor bean
        while(true) {
            try {
                // Block queue from redis until message is received.
                // This will work even with multiple instance of this module.
                SimpleMessage simpleMessage = extEventQueue.take();
                log.info("Message from redis queue received.");
                messageProcessor.processAsync(simpleMessage);
            } catch (InterruptedException e) {
                log.error("Something wrong has happened!", e);
            } catch (Exception e2) {
                log.error("Some random exception", e2);
            }
        }
    }

}
