package com.example.demo; //organizes java classes into a specific namespace

import org.springframework.boot.SpringApplication; //core logic from library
import org.springframework.boot.autoconfigure.SpringBootApplication; // pulls in the SpringBootApplication annotation

// MindMetrics backend entry point 
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}