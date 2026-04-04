package com.codesfree.prueba.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WelcomeController {

    @GetMapping("/")
    public Map<String, String> welcome() {
        return Map.of("message", "Welcome to CodesFree API", "service", "Backend REST API");
    }
}
