package com.equinix.dlaas.service;

import com.equinix.dlaas.domain.FileUploadType;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by ransay on 3/29/2017.
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class ForecastServiceTest {

    @Autowired
    private ForecastService forecastService;

    private String networkId = "vxlVB0sUvw";

    @Test
    public void create() {
        networkId = forecastService.create();
        System.out.println("id: " + networkId);
    }

    @Test
    public void uploadTrain() throws IOException {
        File file = new File("C:/Users/ransay/Desktop/trainraw,txt.txt");
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile("file",
                file.getName(), "text/plain", IOUtils.toByteArray(input));
        forecastService.upload(networkId, multipartFile, FileUploadType.TRAIN_FILE_PATH);
    }

    @Test
    public void uploadTest() throws IOException {
        File file = new File("C:/Users/ransay/Desktop/testraw,txt.txt");
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile("file",
                file.getName(), "text/plain", IOUtils.toByteArray(input));
        forecastService.upload(networkId, multipartFile, FileUploadType.TEST_FILE_PATH);
    }

    @Test
    public void forecast() {
        forecastService.forecast(networkId, 3);
    }
}
