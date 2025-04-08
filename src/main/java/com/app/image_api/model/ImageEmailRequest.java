package com.app.image_api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageEmailRequest {
    private String prompt;
    private String email;
    private String size = "1024x1024";
    private String style = "natural"; // natural (realistyczny) lub vivid (żywy, kreatywny)
} 