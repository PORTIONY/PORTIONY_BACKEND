package com.portiony.portiony.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {
    @Value("${S3_ACCESS_KEY}")
    private String s3AccessKey;

    @Value("${S3_SECRET_KEY}")
    private String s3SecretKey;

    @Value("${S3_REGION:ap-southeast-1}")
    private String s3Region;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(s3Region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(s3AccessKey, s3SecretKey)))
                .build();
    }
}
