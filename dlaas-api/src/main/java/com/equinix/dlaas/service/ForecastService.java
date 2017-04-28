package com.equinix.dlaas.service;

import com.equinix.dlaas.config.NetworkConfig;
import com.equinix.dlaas.domain.*;
import com.equinix.dlaas.util.FileUploadUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.support.collections.RedisList;
import org.springframework.data.redis.support.collections.RedisMap;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Created by ransay on 3/24/2017.
 */

@Service
public class ForecastService {

    @Autowired
    private RedisList<SimpleMessage> eventQueue;

    @Autowired
    private RedisMap<String, SimpleRecord> recordMap;

    @Value("${dataDirectory}")
    private String destination;

    @Autowired
    private NetworkConfig defaultConfig;

    @Autowired
    private Map<String, NetworkService> networks;

    public List<String> forecast(String id, Integer count) {
        SimpleRecord record = recordMap.get(id);
        if (record.getNet() == null)
            throw new RuntimeException("No network configured on this id: " + id);
        NetworkService networkService = NetworkService.getInstance(record.getType(), networks);
        return networkService.predict(record.getNormalizer(), record.getNet(), record.getLastValue(), count,
                record.getConfig());
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

    public String create(CaseType type) {
        SimpleRecord record = new SimpleRecord.SimpleRecordBuilder()
                .id(RandomStringUtils.randomAlphanumeric(10))
                .build();
        NetworkConfig config = new NetworkConfig();
        record.setType(type);
        record.setConfig(config);
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
        eventQueue.add(message);
    }
}
