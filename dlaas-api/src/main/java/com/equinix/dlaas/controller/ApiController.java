package com.equinix.dlaas.controller;

import com.equinix.dlaas.domain.ApiResponse;
import com.equinix.dlaas.domain.FileUploadType;
import com.equinix.dlaas.domain.CaseType;
import com.equinix.dlaas.service.ForecastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
public class ApiController {

    private static final Logger log = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    private ForecastService forecastService;

    @PostMapping("/upload/{id}")
    public ResponseEntity<ApiResponse> upload(@PathVariable String id, @RequestParam("file") MultipartFile file,
                                              @RequestParam("type") FileUploadType type) {
        forecastService.upload(id, file, type);
        return null;
    }

    @PostMapping("/train/{id}")
    public ResponseEntity<ApiResponse> train(@PathVariable String id) {
        forecastService.train(id);
        return null;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> create(@RequestParam("type") CaseType type) {
        String id = forecastService.create(type);
        log.info("Network id: " + id);
        return null;
    }

    @GetMapping("/forecast/{id}")
    public ResponseEntity<ApiResponse> forecast(@PathVariable String id, @RequestParam("count") Integer count) {
        forecastService.forecast(id, count);
        return null;
    }

    @PostMapping("/output/{id}")
    public ResponseEntity<ApiResponse> output(@PathVariable String id,
                                                @RequestParam("payload") Map<String, String> payload) {
        //forecastService.forecast(id, count);
        return null;
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable String id,
                                              @RequestBody List<String> payload) {
        forecastService.update(id, payload);
        return null;
    }

}
