package com.equinix.dlaas.service;

import com.equinix.dlaas.domain.SimpleMessage;
import com.equinix.dlaas.domain.TrainTestMessage;
import com.equinix.dlaas.domain.UpdateMessage;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ransay on 3/30/2017.
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class TrainNetworkServiceTest {

    @Autowired
    private TrainNetworkService trainNetworkService;

    private String networkId = "vxlVB0sUvw";

    @Test
    public void trainTestMessage() {
        TrainTestMessage payload = new TrainTestMessage();
        payload.setNetworkId(networkId);
        SimpleMessage message = new SimpleMessage.SimpleMessageBuilder()
                .id(RandomStringUtils.randomAlphanumeric(10))
                .message(payload)
                .build();
        trainNetworkService.processAsync(message);
    }

    @Test
    public void updateTrainTestMessage() {
        List<String> l = new ArrayList<>();
        l.add("timstamp1;123;456");
        l.add("timstamp2;222;333");
        l.add("timstamp3;444;555");
        UpdateMessage m = new UpdateMessage();
        m.setPayload(l);
        m.setNetworkId(networkId);
        SimpleMessage message = new SimpleMessage.SimpleMessageBuilder()
                .id(RandomStringUtils.randomAlphanumeric(10))
                .message(m)
                .build();
        trainNetworkService.processAsync(message);
    }
}
