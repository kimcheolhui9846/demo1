package com.example.demo.error;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 공통 예외 처리 (WebFlux Reactive)
 * - 반환형은 Mono<ResponseEntity<...>> 로 통일
 * - 컨트롤러/필터 어디에서 던져도 JSON 에러 바디로 응답
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** WebClient 가 HTTP 오류(4xx/5xx)를 받았을 때 */
    @ExceptionHandler(WebClientResponseException.class)
    public Mono<ResponseEntity<Map<String, Object>>> webClientResponse(WebClientResponseException ex) {
        Map<String, Object> body = Map.of(
                "status",  ex.getStatusCode().value(),
                "error",   ex.getStatusCode().toString(),
                // 인코딩 모르면 UTF-8 로 폴백해 문자열 추출
                "message", ex.getResponseBodyAsString(StandardCharsets.UTF_8)
        );
        return Mono.just(ResponseEntity.status(ex.getStatusCode()).body(body));
    }

    /** WebClient 가 연결/타임아웃/네트워크 오류로 아예 응답을 못 받은 경우 */
    @ExceptionHandler(WebClientRequestException.class)
    public Mono<ResponseEntity<Map<String, Object>>> webClientRequest(WebClientRequestException ex) {
        Map<String, Object> body = Map.of(
                "status",  HttpStatus.BAD_GATEWAY.value(),
                "error",   "Bad Gateway",
                "message", ex.getMostSpecificCause() != null
                        ? ex.getMostSpecificCause().getMessage()
                        : ex.getMessage()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body));
    }

    /** Bean Validation: @Valid 바디 바인딩 실패(필드 에러 수집) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Mono<ResponseEntity<Map<String, Object>>> invalidBody(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));

        Map<String, Object> body = Map.of(
                "status",  HttpStatus.BAD_REQUEST.value(),
                "error",   "Bad Request",
                "message", "Validation failed",
                "fields",  fieldErrors
        );
        return Mono.just(ResponseEntity.badRequest().body(body));
    }

    /** Bean Validation: 쿼리파라미터/경로변수 등 제약 위반 */
    @ExceptionHandler(ConstraintViolationException.class)
    public Mono<ResponseEntity<Map<String, Object>>> constraint(ConstraintViolationException ex) {
        Map<String, Object> body = Map.of(
                "status",  HttpStatus.BAD_REQUEST.value(),
                "error",   "Bad Request",
                "message", ex.getMessage()
        );
        return Mono.just(ResponseEntity.badRequest().body(body));
    }

    /** 마지막 방어선: 알 수 없는 예외 → 500 */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> generic(Exception ex) {
        Map<String, Object> body = Map.of(
                "status",  HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "error",   "Internal Server Error",
                "message", ex.getMessage()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body));
    }
}
