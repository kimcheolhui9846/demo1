package com.example.demo.dto;
import java.util.Map;
public record PredictResponse(
        String label,
        double score,
        Map<String, Double> topk
) {}
