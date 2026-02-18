package com.github.fullstackweatherdatacollectionplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

@SpringBootApplication
@EnableScheduling
public class FullstackWeatherDataCollectionPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(FullstackWeatherDataCollectionPlatformApplication.class, args);
    }

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

}
