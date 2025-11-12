package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;
import reactor.util.retry.Retry.RetrySignal;

import java.time.Duration;

@Configuration
public class RetryConfig {

    @Bean
    public RetryBackoffSpec backoffSpec() {
        return Retry
                .backoff(3, Duration.ofSeconds(1))   // 최대 3회, 1초부터 지수 백오프
                .maxBackoff(Duration.ofSeconds(5))   // 최대 대기 5초
                .transientErrors(true)               // 일시적 오류만 재시도
                .doAfterRetry(this::logRetry)        // 재시도 로그 (선택)
                .onRetryExhaustedThrow((spec, signal) -> {
                    // 최종 실패를 원인 예외로 전파
                    Throwable failure = signal.failure();
                    return failure != null ? failure
                            : new IllegalStateException("Retry exhausted");
                });
    }

    private void logRetry(RetrySignal signal) {
        System.out.printf(
                "[RETRY] attempt=%d cause=%s%n",
                signal.totalRetries() + 1, // 이번 시도 번호(0-base라 +1)
                signal.failure() != null ? signal.failure() : "unknown"
        );
    }
}

