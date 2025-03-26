package com.app.image_api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DallEResponse {
    private List<DallEImage> data;

    @Data
    public static class DallEImage {
        private String url;
        @JsonProperty("revised_prompt")
        private String revisedPrompt;
    }
} 