package com.grocerydeliveryapp.controller;

import com.grocerydeliveryapp.dto.auth.*;
import com.grocerydeliveryapp.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}", maxAge = 3600)
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<AuthResponse> resendOtp(@RequestParam String email) {
        return ResponseEntity.ok(authService.resendOtp(email));
    }

    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailAvailability(@RequestParam String email) {
        // This endpoint can be used by the frontend to check email availability during registration
        try {
            authService.resendOtp(email);
            return ResponseEntity.ok(false); // Email exists
        } catch (RuntimeException e) {
            return ResponseEntity.ok(true); // Email is available
        }
    }

    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsernameAvailability(@RequestParam String username) {
        // This endpoint can be used by the frontend to check username availability during registration
        // Implementation would depend on your UserRepository having a method to check username existence
        return ResponseEntity.ok(true); // Placeholder response
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<AuthResponse> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.badRequest()
                .body(new AuthResponse(e.getMessage()));
    }
}
