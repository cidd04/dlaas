package com.equinix.dlaas.service;

import com.equinix.dlaas.domain.*;
import com.equinix.dlaas.util.TrainNetworkUtil;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.support.collections.RedisList;
import org.springframework.data.redis.support.collections.RedisMap;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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

    @Autowired
    Map<String, NetworkService> networks;

    @Value("${dataDirectory}")
    private String dataDirectory;

    @Override
    public void processAsync(SimpleMessage simpleMessage) {
        try {
            if (simpleMessage.getMessage() instanceof FileUploadMessage) {
                processFileUpload(simpleMessage);
            } else if (simpleMessage.getMessage() instanceof TrainTestMessage) {
                processTrainTest(simpleMessage);
            } else if (simpleMessage.getMessage() instanceof UpdateMessage) {
                processUpdate(simpleMessage);
            }
            //throw new RuntimeException("ABCD");
        } catch (Exception e) {
            throw new RuntimeException(e);
            //2. Retry using scheduler
//            log.info("Some exception occurred. Retrying...", e.getMessage());
//            simpleMessage.setRetryCount(simpleMessage.getRetryCount() + 1);
//            simpleMessage.setStatus(SimpleMessageStatus.RETRY);
//            if (simpleMessage.getRetryCount() < retryMaxCount) {
//                this.retry(simpleMessage);
//            } else {
//                simpleMessage.setStatus(SimpleMessageStatus.FAILED);
//            }
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

    private void processFileUpload(SimpleMessage simpleMessage) throws IOException {
        //1. Get file name
        FileUploadMessage message = (FileUploadMessage) simpleMessage.getMessage();
        //2. Unzip
        TrainNetworkUtil.unzip(dataDirectory + "/" + message.getFileName(), dataDirectory);
        //3. Send notification into queue
        SimpleMessage notify = new SimpleMessage.SimpleMessageBuilder().build();
        notifyQueue.add(notify);
    }

    private void processTrainTest(SimpleMessage simpleMessage) throws IOException, InterruptedException {
        //1. Start Training
        TrainTestMessage message = (TrainTestMessage) simpleMessage.getMessage();
        SimpleRecord record = recordMap.get(message.getNetworkId());
        record.setTrainFilePath(record.getId() + "_0_train.txt");
        TrainNetworkUtil.formatRawData(dataDirectory + "/" + record.getRawTrainFilePath(),
                dataDirectory + "/" + record.getTrainFilePath());
        int columnCount = TrainNetworkUtil.countColumn(dataDirectory + "/" + record.getTrainFilePath());
        record.getConfig().setColumnCount(columnCount);
        MultiLayerNetwork net;
        NormalizerMinMaxScaler normalizer;
        NetworkService networkService = NetworkService.getInstance(record.getType(), networks);
        normalizer = networkService.createNormalizer();
        normalizer.fitLabel(true);
        if (record.getRawTestFilePath() != null) {
            record.setTestFilePath(record.getId() + "_0_test.txt");
            TrainNetworkUtil.formatRawData(dataDirectory + "/" + record.getRawTestFilePath(),
                    dataDirectory + "/" + record.getTestFilePath());
            net = networkService.createNetwork(normalizer, dataDirectory + "/" + record.getId()
                    + "_%d_train.txt", dataDirectory + "/" + record.getId()
                    + "_%d_test.txt", record.getConfig());
        } else {
            net = networkService.createNetwork(normalizer, dataDirectory + "/" + record.getId()
                    + "_%d_train.txt", record.getConfig());
        }
        //2. Set last value from train data
        List<String> lastValue = TrainNetworkUtil.getLastValue(
                dataDirectory + "/" + record.getTrainFilePath());
        record.setLastValue(lastValue);
        record.setNet(net);
        record.setNormalizer(normalizer);
        recordMap.put(record.getId(), record);
        //3. Send notification into queue
        SimpleMessage notify = new SimpleMessage.SimpleMessageBuilder().build();
        notifyQueue.add(notify);
    }

    private void processUpdate(SimpleMessage simpleMessage) {
        UpdateMessage message = (UpdateMessage) simpleMessage.getMessage();
        SimpleRecord record = recordMap.get(message.getNetworkId());
        if (record.getNet() == null)
            throw new RuntimeException("No network configured on this id: " + record.getId());
        List<String> payload = TrainNetworkUtil.formatRawData(message.getPayload());
        NetworkService networkService = NetworkService.getInstance(record.getType(), networks);
        MultiLayerNetwork net = networkService.updateNetwork(record.getNormalizer(), record.getNet(), payload,
                record.getConfig());
        //2. Set last value from payload
        record.setLastValue(payload);
        //3. Update record map
        record.setNet(net);
        recordMap.put(record.getId(), record);
        //4. Send notification into queue
        SimpleMessage notify = new SimpleMessage.SimpleMessageBuilder().build();
        notifyQueue.add(notify);
    }
}
