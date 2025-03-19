package com.grocerydeliveryapp.service;

import com.grocerydeliveryapp.dto.auth.*;
import com.grocerydeliveryapp.model.User;
import com.grocerydeliveryapp.repository.UserRepository;
import com.grocerydeliveryapp.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setRoles(Collections.singleton("USER"));
        user.setEmailVerified(false);

        // Generate and set OTP
        String otp = generateOTP();
        user.setOtp(otp);
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(10));

        // Save user
        userRepository.save(user);

        // Send verification email
        sendVerificationEmail(user.getEmail(), otp);

        return new AuthResponse("Registration successful. Please verify your email.");
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
            );

            User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .orElseGet(() -> userRepository.findByEmail(request.getUsernameOrEmail())
                    .orElseThrow(() -> new RuntimeException("User not found")));

            if (!user.isEmailVerified()) {
                // Generate new OTP and send email
                String newOtp = generateOTP();
                user.setOtp(newOtp);
                user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(10));
                userRepository.save(user);
                sendVerificationEmail(user.getEmail(), newOtp);

                return new AuthResponse("Please verify your email first. A new verification code has been sent.");
            }

            String token = jwtUtil.generateToken(authentication.getPrincipal());

            return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles(),
                user.isEmailVerified()
            );

        } catch (Exception e) {
            throw new RuntimeException("Invalid username/email or password");
        }
    }

    @Transactional
    public AuthResponse verifyOtp(OtpRequest request) {
        User user = userRepository.findByEmailAndOtp(request.getEmail(), request.getOtp())
            .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        if (user.getOtpExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired");
        }

        user.setEmailVerified(true);
        user.setOtp(null);
        user.setOtpExpiryTime(null);
        userRepository.save(user);

        String token = jwtUtil.generateToken(
            org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities("ROLE_USER")
                .build()
        );

        return new AuthResponse(
            token,
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRoles(),
            true
        );
    }

    @Transactional
    public AuthResponse resendOtp(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        String newOtp = generateOTP();
        user.setOtp(newOtp);
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        sendVerificationEmail(email, newOtp);

        return new AuthResponse("New OTP has been sent to your email");
    }

    private String generateOTP() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }

    private void sendVerificationEmail(String email, String otp) {
        String subject = "Email Verification - Grocery Delivery App";
        String body = String.format(
            "Hello,\n\nYour verification code is: %s\n\n" +
            "This code will expire in 10 minutes.\n\n" +
            "If you didn't request this code, please ignore this email.\n\n" +
            "Best regards,\nGrocery Delivery App Team",
            otp
        );
        emailService.sendEmail(email, subject, body);
    }
}
