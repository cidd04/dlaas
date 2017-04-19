package com.equinix.dlaas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Created by ransay on 3/30/2017.
 */

@Configuration
@EnableSwagger2
public class NotifyConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
