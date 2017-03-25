package com.equinix.dlaas.service;

import com.equinix.dlaas.domain.*;
import com.equinix.dlaas.util.ZipUtil;
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
public class TrainNetworkService implements MessageProcessor {

    private static final Logger log = LoggerFactory.getLogger(TrainNetworkService.class);

    @Autowired
    private RedisList<SimpleMessage> eventQueue;

    @Autowired
    private RedisMap<String, SimpleRecord> recordMap;

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
    MultiFeatureSequenceService multiFeatureSequenceService;

    @Override
    public void processAsync(SimpleMessage simpleMessage) {
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
                TrainTestMessage message = mapper.convertValue(simpleMessage.getMessage(), TrainTestMessage.class);
                SimpleRecord record = recordMap.get(message.getId());
                multiFeatureSequenceService.process(record.getTrainFilePath(), record.getTestFilePath());
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
