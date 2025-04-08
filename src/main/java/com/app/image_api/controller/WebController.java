package com.app.image_api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    @GetMapping("/text-analysis")
    public String textAnalysis() {
        return "text-analysis";
    }
    
    @GetMapping("/gif-generator")
    public String gifGenerator() {
        return "gif-generator";
    }
} 