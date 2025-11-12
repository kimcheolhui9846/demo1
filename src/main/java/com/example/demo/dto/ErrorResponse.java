package com.example.demo.dto;

public record ErrorResponse(
        String requestId,
        String message
) {}
