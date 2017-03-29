package com.equinix.dlaas.controller;

import com.equinix.dlaas.domain.EventResponse;
import com.equinix.dlaas.domain.FileUploadType;
import com.equinix.dlaas.service.ForecastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class EventController {

    private static final Logger log = LoggerFactory.getLogger(EventController.class);

    @Autowired
    private ForecastService forecastService;

    @PostMapping("/upload/{id}")
    public ResponseEntity<EventResponse> upload(@PathVariable String id, @RequestParam("file") MultipartFile file,
                                                @RequestParam("type") FileUploadType type) {
        forecastService.upload(id, file,type);
        return null;
    }

    @PostMapping("/train/{id}")
    public ResponseEntity<EventResponse> train(@PathVariable String id) {
        forecastService.train(id);
        return null;
    }

    @PostMapping("/create")
    public ResponseEntity<EventResponse> create() {
        String id = forecastService.create();
        return null;
    }

    @GetMapping("/forecast/{id}")
    public ResponseEntity<EventResponse> forecast(@PathVariable String id, @RequestParam("count") Integer count) {
        forecastService.forecast(id, count);
        return null;
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<EventResponse> update(@PathVariable String id,
                                                             @RequestBody List<String> payload) {
        forecastService.update(id, payload);
        return null;
    }

}
