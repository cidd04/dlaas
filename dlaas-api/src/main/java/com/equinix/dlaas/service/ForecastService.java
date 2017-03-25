package com.equinix.dlaas.service;

import com.equinix.dlaas.domain.SimpleRecord;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.support.collections.RedisMap;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Created by ransay on 3/24/2017.
 */

@Service
public class ForecastService {

    @Autowired
    private RedisMap<String, SimpleRecord> recordMap;

    public void process(String id, List<String> payload) {
        MultiLayerNetwork net = null;
        SimpleRecord record = recordMap.get(id);
        if (record.getNet() == null) {
            try {
                net = ModelSerializer.restoreMultiLayerNetwork(record.getTrainFilePath());
            } catch (IOException e) {
                throw new RuntimeException("G");
            }
            record.setNet(net);
            recordMap.put(id, record);
        } else {
            net = record.getNet();
        }
    }
}
