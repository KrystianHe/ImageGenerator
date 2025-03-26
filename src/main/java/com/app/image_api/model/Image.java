package com.app.image_api.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "images")
public class Image {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String prompt;
    
    @Column(nullable = false)
    private String style;
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(columnDefinition = "TEXT")
    private String imageData;
    
    @Column(nullable = false)
    private String contentType = "image/gif";
} 