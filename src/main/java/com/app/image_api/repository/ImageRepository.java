package com.app.image_api.repository;

import com.app.image_api.model.GeneratedImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<GeneratedImage, Long> {
    
    List<GeneratedImage> findByPromptContainingIgnoreCase(String prompt);
    
    List<GeneratedImage> findByStyle(String style);
    
    List<GeneratedImage> findTop10ByOrderByCreatedAtDesc();
} 