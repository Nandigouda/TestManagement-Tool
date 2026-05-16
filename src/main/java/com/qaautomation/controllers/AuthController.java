package com.qaautomation.controllers;

import com.qaautomation.dto.AuthResponse;
import com.qaautomation.dto.LoginRequest;
import com.qaautomation.dto.SignupRequest;
import com.qaautomation.services.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * User login endpoint
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());
        
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(AuthResponse.builder()
                    .success(false)
                    .message("Username is required")
                    .build());
        }
        
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(AuthResponse.builder()
                    .success(false)
                    .message("Password is required")
                    .build());
        }
        
        AuthResponse response = authService.login(request);
        return response.getSuccess() 
            ? ResponseEntity.ok(response) 
            : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    /**
     * User signup/registration endpoint
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request) {
        log.info("Signup attempt for user: {}", request.getUsername());
        
        // Validate input
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(AuthResponse.builder()
                    .success(false)
                    .message("Username is required")
                    .build());
        }
        
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(AuthResponse.builder()
                    .success(false)
                    .message("Email is required")
                    .build());
        }
        
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            return ResponseEntity.badRequest()
                .body(AuthResponse.builder()
                    .success(false)
                    .message("Password must be at least 6 characters")
                    .build());
        }
        
        AuthResponse response = authService.signup(request);
        return response.getSuccess() 
            ? ResponseEntity.status(HttpStatus.CREATED).body(response)
            : ResponseEntity.badRequest().body(response);
    }
}
