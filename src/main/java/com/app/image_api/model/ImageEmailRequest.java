package com.app.image_api.model;

import lombok.Data;

@Data
public class ImageEmailRequest {
    private String prompt;
    private String email;
    private String size = "1024x1024";
    private String style = "natural"; // natural (realistyczny) lub vivid (żywy, kreatywny)
    private String quality = "standard";
} 