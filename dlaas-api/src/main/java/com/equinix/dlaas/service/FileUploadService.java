package com.equinix.dlaas.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by ransay on 3/24/2017.
 */

@Service
public class FileUploadService {

    public void upload(MultipartFile file) {
        if (file.isEmpty()) {
            // throw error
            return;
        }
        try {
            // Get the file and save it somewhere
            byte[] bytes = file.getBytes();
            Path path = Paths.get("" + file.getOriginalFilename());
            Files.write(path, bytes);
            //log success
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
