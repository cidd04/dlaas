package com.equinix.dlaas.service;

import com.equinix.dlaas.domain.FileUploadType;
import com.equinix.dlaas.domain.SimpleMessage;
import com.equinix.dlaas.domain.SimpleRecord;
import com.equinix.dlaas.domain.UpdateMessage;
import com.equinix.dlaas.util.FileUploadUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.support.collections.RedisList;
import org.springframework.data.redis.support.collections.RedisMap;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Created by ransay on 3/24/2017.
 */

@Service
public class ForecastService {

    @Autowired
    private NetworkService networkService;

    @Autowired
    private RedisList<SimpleMessage> eventQueue;

    @Autowired
    private RedisMap<String, SimpleRecord> recordMap;

    @Value("${dataDirectory}")
    private String destination;

    public List<String> forecast(String id, Integer count) {
        SimpleRecord record = recordMap.get(id);
        if (record.getNet() == null)
            throw new RuntimeException("No network configured on this id: " + id);
        return networkService.predict(record.getNet(), record.getLastValue(), count);
    }

    public void update(String id, List<String> payload) {
        UpdateMessage updateMessage = new UpdateMessage();
        updateMessage.setNetworkId(id);
        updateMessage.setPayload(payload);
        SimpleMessage message = new SimpleMessage.SimpleMessageBuilder()
                .id(RandomStringUtils.randomAlphanumeric(10))
                .message(updateMessage)
                .build();
        eventQueue.add(message);
    }

    public String create() {
        SimpleRecord record = new SimpleRecord.SimpleRecordBuilder()
                .id(RandomStringUtils.randomAlphanumeric(10))
                .build();
        recordMap.put(record.getId(), record);
        return record.getId();
    }

    public void upload(String id, MultipartFile file, FileUploadType type) {
        FileUploadUtil.upload(file, destination);
        SimpleRecord record = recordMap.get(id);
        if (record == null)
            throw new RuntimeException("No record found for this id: " + id);
        if (type == FileUploadType.TRAIN_FILE_PATH) {
            record.setRawTrainFilePath(file.getOriginalFilename());
        } else {
            record.setRawTestFilePath(file.getOriginalFilename());
        }
        recordMap.put(id, record);
    }

    public void train(String id) {
        SimpleMessage message = new SimpleMessage.SimpleMessageBuilder()
                .id(RandomStringUtils.randomAlphanumeric(10))
                .build();
        SimpleRecord record = recordMap.get(id);
        if (record.getNet() == null)
            throw new RuntimeException("No network configured on this id: " + id);
        if (record.getTrainFilePath() == null)
            throw new RuntimeException("Train data not found!");
        if (record.getTestFilePath() == null)
            throw new RuntimeException("Test data not found!");
        eventQueue.add(message);
    }
}
