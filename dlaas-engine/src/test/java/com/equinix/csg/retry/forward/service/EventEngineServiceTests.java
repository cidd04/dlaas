package com.equinix.csg.retry.forward.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by ransay on 2/10/2017.
 */

@RunWith(MockitoJUnitRunner.class)
public class EventEngineServiceTests {

    @Test
    public void testProcessSuccess() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader("C:/Users/ransay/Desktop/trainraw,txt.txt"));
             FileWriter fw = new FileWriter("C:/Users/ransay/Desktop/new.txt")) {
            String line;
            String[] s = null;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                s = line.split(";");
                if (!first)
                    fw.write(s[1] + "\n");
                for (int i = 1; i < s.length; i++) {
                    fw.write(s[i] + ";");
                }
                first = false;
            }
            fw.write(s[1] + "\n");
        }
    }
}
