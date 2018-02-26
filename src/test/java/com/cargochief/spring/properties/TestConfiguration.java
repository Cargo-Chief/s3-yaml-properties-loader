package com.cargochief.spring.properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@Configuration
@S3YamlPropertiesLocation("${AWS_S3_CONFIG_LOCATION}")
@ComponentScan("com.cargochief.spring.properties")
public class TestConfiguration {

    @Bean
    public AmazonS3 getAmazonS3() {
        return AmazonS3ClientBuilder
                .standard()
                .build();
    }
}
