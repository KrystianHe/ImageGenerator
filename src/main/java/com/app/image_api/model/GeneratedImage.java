package com.app.image_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "generated_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedImage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String imageUrl;
    
    @Column(nullable = false)
    private String style;
    
    @Column(nullable = false)
    private String quality;
    
    @Column(nullable = false)
    private String size;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "file_name")
    private String fileName;
    
    @Column(name = "content_type")
    private String contentType;
    
    @Lob
    @Column(name = "image_data", columnDefinition = "LONGTEXT")
    private String imageData;
    
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
} 