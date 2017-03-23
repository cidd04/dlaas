package com.equinix.dlaas.engine.service;

import com.equinix.dlaas.engine.domain.FileUploadMessage;
import com.equinix.dlaas.engine.domain.SimpleMessage;
import com.equinix.dlaas.engine.domain.SimpleMessageStatus;
import com.equinix.dlaas.engine.domain.TrainTestMessage;
import com.equinix.dlaas.engine.util.ZipUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.support.collections.RedisList;
import org.springframework.data.redis.support.collections.RedisMap;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by ransay on 2/10/2017.
 */

@Component
public class EventEngineService {

    private static final Logger log = LoggerFactory.getLogger(EventEngineService.class);

    @Autowired
    private RedisList<SimpleMessage> eventQueue;

    @Autowired
    private RedisMap<String, SimpleMessage> eventMap;

    @Autowired
    private RedisList<SimpleMessage> notifyQueue;

    @Value("${retry.maxCount}")
    private int retryMaxCount;

    @Autowired
    private ScheduledExecutorService localExecutor;

    @Value("${retry.timeoutInSeconds}")
    private int timeoutInSeconds;

    @Value("${directory.zip}")
    private String zipDirectory;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    MultiFeatureSequence multiFeatureSequence;

    public void process(SimpleMessage simpleMessage) {
        try {
            String messageClass = simpleMessage.getMessageClass();
            if (messageClass.equals(FileUploadMessage.class.getCanonicalName())) {
                //1. Get file name
                FileUploadMessage message = mapper.convertValue(simpleMessage.getMessage(), FileUploadMessage.class);
                //2. Unzip
                ZipUtil.unzip(zipDirectory + File.separator + message.getFileName(), zipDirectory);
                //3. Send notification into queue
                SimpleMessage notify = new SimpleMessage.SimpleMessageBuilder().build();
                notifyQueue.add(notify);
            } else if (messageClass.equals(TrainTestMessage.class.getCanonicalName())) {
                //1. Start Training
                multiFeatureSequence.process();
                //2. Send notification into queue
                SimpleMessage notify = new SimpleMessage.SimpleMessageBuilder().build();
                notifyQueue.add(notify);
            }
            //throw new RuntimeException("ABCD");
        } catch (Exception e) {
            //2. Retry using scheduler
            log.info("Some exception occurred", e.getMessage());
            simpleMessage.setRetryCount(simpleMessage.getRetryCount() + 1);
            simpleMessage.setStatus(SimpleMessageStatus.RETRY);
            if (simpleMessage.getRetryCount() < retryMaxCount) {
                this.retry(simpleMessage);
            } else {
                simpleMessage.setStatus(SimpleMessageStatus.FAILED);
            }
        }
        //3. Save in DB
        // mongoTemplate.save(simpleMessage);
        // DefaultRedisList has many unsupported operations.
        // In order to modify a value in the list, we have to iterate
        // it manually and set it using the index
        // DefaultRedisMap is thread safe because redistemplate is also thread safe
        eventMap.put(simpleMessage.getId(), simpleMessage);
    }

    private void retry(SimpleMessage simpleMessage) {
        localExecutor.schedule(() -> {
            try {
                eventQueue.put(simpleMessage);
                log.info("Send Forward Success");
            } catch (InterruptedException e) {
                log.info("Interrupted Exception");
                throw new RuntimeException(e);
            }
        }, timeoutInSeconds, TimeUnit.SECONDS);
    }
}
