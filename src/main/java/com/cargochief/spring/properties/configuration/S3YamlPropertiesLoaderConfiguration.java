package com.cargochief.spring.properties.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.services.s3.AmazonS3;
import com.spring.loader.cloud.S3Service;
import com.spring.loader.cloud.S3StreamLoader;

@Configuration
public class S3YamlPropertiesLoaderConfiguration {

    @Bean
    S3Service s3Service(AmazonS3 amazonS3) {
        return new S3Service(amazonS3);
    }

    @Bean
    S3StreamLoader s3ResourceLoader(S3Service s3Service) {
        return new S3StreamLoader(s3Service);
    }

}
