package com.movelog.global.config.security;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmazonS3Config {

    @Value("${cloud.aws.credentials.access-key}")
    private String awsAccessKeyId;

    @Value("${cloud.aws.credentials.secret-key}")
    private String awsSecretKey;

    @Value("${cloud.aws.region.static}")
    private String awsRegion;

    @Value("${cloud.aws.s3.bucketName}")
    private String bucketName;

    @Bean
    public AmazonS3 amazonS3() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey);
        return AmazonS3ClientBuilder.standard()
                .withRegion(awsRegion)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }

    @Bean
    public String bucketName() {
        return bucketName;
    }
}
