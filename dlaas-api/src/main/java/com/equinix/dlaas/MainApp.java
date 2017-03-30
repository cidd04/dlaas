package com.equinix.dlaas;

/**
 * Created by ransay on 1/18/2017.
 */

import com.equinix.dlaas.service.MessageProcessor;
import com.equinix.dlaas.service.RedisMessageReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MainApp implements CommandLineRunner {

    @Autowired
    private RedisMessageReceiver redisMessageReceiver;

    @Autowired
    private MessageProcessor notifyReceiverService;

    public static void main(String[] args) {
        SpringApplication.run(MainApp.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        redisMessageReceiver.listen(notifyReceiverService);
    }
}