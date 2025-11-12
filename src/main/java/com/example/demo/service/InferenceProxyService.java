package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import reactor.util.retry.RetryBackoffSpec;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InferenceProxyService {

    private final WebClient inferenceWebClient;   // InferenceClientConfig @Bean
    private final RetryBackoffSpec backoffSpec;   // RetryConfig @Bean

    /** 파일 업로드 → 추론 서버 프록시 */
    public Mono<String> predictByFile(FilePart file) {
        // content-type 널 가드
        MediaType ct = Optional.ofNullable(file.headers().getContentType())
                .orElse(MediaType.IMAGE_JPEG);

        MultipartBodyBuilder mb = new MultipartBodyBuilder();
        mb.part("file", file)
                .filename(file.filename())
                .contentType(ct);

        MultiValueMap<String, org.springframework.http.HttpEntity<?>> body = mb.build();

        return inferenceWebClient.post()
                .uri("/predict")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(body))
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(backoffSpec); // ★ 재시도/백오프 적용
    }

    /** URL로 추론 */
    public Mono<String> predictByUrl(String imageUrl) {
        record UrlBody(String url) {}
        return inferenceWebClient.post()
                .uri("/predict-by-url")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UrlBody(imageUrl))
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(backoffSpec);
    }

    /** 라벨 목록 */
    public Mono<String> labels() {
        return inferenceWebClient.get()
                .uri("/labels")
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(backoffSpec);
    }
}


