package com.example.shop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 정산 Batch가 비동기 실행될 때 사용할 ThreadPoolTaskExecutor 설정.
 */
@Configuration
public class BatchAsyncConfig {

    @Value("${settlement.async.pool-size:5}")
    private int poolSize;

    @Bean("settlementTaskExecutor")
    public ThreadPoolTaskExecutor settlementTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);
        executor.setThreadNamePrefix("settlement-");
        executor.initialize();
        return executor;
    }
}