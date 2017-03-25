package com.equinix.dlaas.controller;

import com.equinix.dlaas.domain.EventResponse;
import com.equinix.dlaas.service.FileUploadService;
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

    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping("/upload")
    public ResponseEntity<EventResponse> upload(@RequestParam("file") MultipartFile file) {
        fileUploadService.upload(file);
        return null;
    }

    @GetMapping("/forecast/{id}")
    public ResponseEntity<EventResponse> forecast(@PathVariable String id) {
        forecastService.process(id, null);
        return null;
    }

    @PostMapping("/forecast/{id}")
    public ResponseEntity<EventResponse> forecastWithPayload(@PathVariable String id,
                                                             @RequestBody List<String> payload) {
        forecastService.process(id, payload);
        return null;
    }

}
