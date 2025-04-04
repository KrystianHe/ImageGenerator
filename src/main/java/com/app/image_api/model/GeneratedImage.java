package com.app.image_api.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "images")
@Data
public class GeneratedImage {
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
    private String contentType;

    @Column(nullable = false)
    private String size;

    @Column(nullable = false)
    private String quality;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 