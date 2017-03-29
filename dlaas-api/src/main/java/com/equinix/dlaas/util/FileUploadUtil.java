package com.equinix.dlaas.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by ransay on 3/28/2017.
 */
public class FileUploadUtil {

    public static void upload(MultipartFile file) {
        if (file.isEmpty()) {
            return;
        }
        try {
            // Get the file and save it somewhere
            byte[] bytes = file.getBytes();
            Path path = Paths.get("" + file.getOriginalFilename());
            Files.write(path, bytes);
            //log success
        } catch (IOException e) {
           throw new RuntimeException("Upload Fail! ", e);
        }
    }
}
