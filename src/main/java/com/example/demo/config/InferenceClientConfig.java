package com.example.demo.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Qualifier;

import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Configuration
public class InferenceClientConfig {


    /** 추론 서버 호출용 WebClient Bean */
    @Bean
    public WebClient inferenceWebClient(
            WebClient.Builder builder,
            @Qualifier("logFilter") ExchangeFilterFunction logFilter, // ← ClientLoggingConfig의 logFilter 주입
            @Value("${inference.base-url}") String baseUrl) {

        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(5))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(5))
                        .addHandlerLast(new WriteTimeoutHandler(5)));

        return builder
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(logFilter) // ← 주입한 필터 적용
                .defaultHeaders(h -> {
                    h.setAccept(List.of(MediaType.APPLICATION_JSON));
                    h.setAcceptCharset(List.of(StandardCharsets.UTF_8));
                })
                .build();
    }
}


