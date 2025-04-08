package com.app.image_api.repository;

import com.app.image_api.model.GeneratedGif;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GifRepository extends JpaRepository<GeneratedGif, Long> {
    List<GeneratedGif> findTop10ByOrderByCreatedAtDesc();
    
    List<GeneratedGif> findByPromptContainingIgnoreCase(String prompt);
    
    List<GeneratedGif> findByStyle(String style);
} 