package com.example.demo.controller;

import com.example.demo.dto.DoctorConnectionRequest;
import com.example.demo.dto.DoctorConnectionResponse;
import com.example.demo.service.DoctorConnectionService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/doctor-connections")
public class DoctorConnectionController {

    private final DoctorConnectionService doctorConnectionService;

    public DoctorConnectionController(DoctorConnectionService doctorConnectionService) {
        this.doctorConnectionService = doctorConnectionService;
    }

    @GetMapping
    public List<DoctorConnectionResponse> getConnections(Authentication authentication) {
        return doctorConnectionService.getPatientConnections(authentication.getName());
    }

    @PostMapping
    public ResponseEntity<DoctorConnectionResponse> createConnection(
        Authentication authentication,
        @RequestBody DoctorConnectionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(doctorConnectionService.createConnection(authentication.getName(), request));
    }

    @PostMapping("/{connectionId}/revoke")
    public DoctorConnectionResponse revokeConnection(
        @PathVariable Long connectionId,
        Authentication authentication
    ) {
        return doctorConnectionService.revokeConnection(authentication.getName(), connectionId);
    }

    @PostMapping("/{connectionId}/reactivate")
    public DoctorConnectionResponse reactivateConnection(
        @PathVariable Long connectionId,
        Authentication authentication
    ) {
        return doctorConnectionService.reactivateConnection(authentication.getName(), connectionId);
    }
}
