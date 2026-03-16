package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clinician")
public class ClinicianController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "Protected clinician dashboard endpoint";
    }
}