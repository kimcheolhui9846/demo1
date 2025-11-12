package com.example.demo.controller;

import com.example.demo.service.InferenceProxyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class PredictController {

    private final InferenceProxyService svc;

    /** POST /api/v1/predict (multipart/form-data, file=이미지) */
    @PostMapping(path = "/predict", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<String> predict(@RequestPart("file") FilePart file) {
        return svc.predictByFile(file);
    }

    /** POST /api/v1/predict-by-url  { "url": "http://..." } */
    @PostMapping(path = "/predict-by-url", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> predictByUrl(@RequestBody UrlRequest body) {
        return svc.predictByUrl(body.url());
    }

    /** GET /api/v1/labels */
    @GetMapping("/labels")
    public Mono<String> labels() {
        return svc.labels();
    }

    /** record 기반 간단 DTO */
    public record UrlRequest(String url) {}
}
