package com.equinix.dlaas.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.equinix.dlaas.api.domain.EventResponse;
import com.equinix.dlaas.api.domain.SimpleMessage;
import com.equinix.dlaas.api.service.EventReceiverService;

import javax.validation.Valid;

@RestController
public class EventController {

    private static final Logger log = LoggerFactory.getLogger(EventController.class);

    @Autowired
    private EventReceiverService eventReceiverService;

    @PostMapping("/upload")
    public ResponseEntity<EventResponse> upload(@Valid @RequestBody String request) {
        SimpleMessage simpleMessage = new SimpleMessage();
        eventReceiverService.process(simpleMessage);
        return  null;
    }

    @GetMapping("/forecast")
    public ResponseEntity<EventResponse> forecast(@Valid @RequestBody String request) {
        SimpleMessage simpleMessage = new SimpleMessage();
        eventReceiverService.process(simpleMessage);
        return  null;
    }

}
