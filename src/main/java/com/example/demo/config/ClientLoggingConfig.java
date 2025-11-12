package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

@Configuration
public class ClientLoggingConfig {

    @Bean
    public ExchangeFilterFunction logFilter() {
        return ExchangeFilterFunction
                .ofRequestProcessor(req -> {
                    System.out.println("[REQ] " + req.method() + " " + req.url());
                    return Mono.just(req);
                })
                .andThen(
                        ExchangeFilterFunction.ofResponseProcessor(res -> {
                            // Spring 6.x: statusCode() -> HttpStatusCode, 값은 value()
                            System.out.println("[RES] " + res.statusCode().value());
                            return Mono.just(res);
                        })
                );
    }
}
