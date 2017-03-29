package com.equinix.dlaas.service;

import com.equinix.dlaas.domain.SimpleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Created by ransay on 2/4/2017.
 */
@Service
public class NotifyReceiverService implements MessageProcessor {

    private static final Logger log = LoggerFactory.getLogger(NotifyReceiverService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Async
    public void processAsync(SimpleMessage simpleMessage) {
        restTemplate.postForEntity(null, null, NotifyReceiverService.class);
    }

}
