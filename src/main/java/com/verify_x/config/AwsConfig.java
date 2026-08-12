package com.verify_x.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.textract.TextractClient;

@Configuration
public class AwsConfig {

    @Bean
    public TextractClient textractClient() {

        return TextractClient.builder()
                .region(Region.AP_SOUTH_1)
                .build();
    }
}