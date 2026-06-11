package com.example.demo.controller;

import com.example.demo.dto.ParticipantProfileResponse;
import com.example.demo.dto.ParticipantProfileUpdateRequest;
import com.example.demo.service.ParticipantProfileService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final ParticipantProfileService participantProfileService;

    public UserController(ParticipantProfileService participantProfileService) {
        this.participantProfileService = participantProfileService;
    }

    @GetMapping("/profile")
    public ParticipantProfileResponse getProfile(Authentication authentication) {
        return participantProfileService.getOwnProfile(authentication.getName());
    }

    @PutMapping("/profile")
    public ParticipantProfileResponse updateProfile(
        Authentication authentication,
        @RequestBody ParticipantProfileUpdateRequest request
    ) {
        return participantProfileService.updateOwnProfile(authentication.getName(), request);
    }
}
