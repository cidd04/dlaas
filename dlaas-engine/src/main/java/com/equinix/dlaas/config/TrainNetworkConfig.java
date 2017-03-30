package com.equinix.dlaas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by ransay on 3/30/2017.
 */

@Configuration
public class TrainNetworkConfig {

    @Bean
    public ScheduledExecutorService localExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }

}
