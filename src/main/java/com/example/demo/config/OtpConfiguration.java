package com.example.demo.config;

import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(OtpProperties.class)
public class OtpConfiguration {
    @Bean
    RestClient otpRestClient(OtpProperties properties) {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(client);
        requestFactory.setReadTimeout(properties.readTimeout());
        return RestClient.builder().requestFactory(requestFactory).build();
    }

    @Bean
    Clock otpClock() {
        return Clock.systemUTC();
    }
}
