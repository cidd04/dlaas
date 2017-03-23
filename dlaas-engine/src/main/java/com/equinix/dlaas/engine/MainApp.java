package com.equinix.dlaas.engine;

import com.equinix.dlaas.engine.service.RedisMessageReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by ransay on 2/10/2017.
 */
@SpringBootApplication
public class MainApp implements CommandLineRunner {

    @Autowired
    private RedisMessageReceiver redisMessageReceiver;

    public static void main(String[] args) {
        SpringApplication.run(MainApp.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        redisMessageReceiver.listen();
    }

}