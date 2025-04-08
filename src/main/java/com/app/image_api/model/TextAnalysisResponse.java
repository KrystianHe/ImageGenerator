package com.app.image_api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextAnalysisResponse {
    private String summary;
    private List<String> keyPoints;
    private Map<String, Integer> sentimentAnalysis;
    private Map<String, Object> additionalInfo;
} 