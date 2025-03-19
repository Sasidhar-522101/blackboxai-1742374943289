package com.grocerydeliveryapp.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private Set<String> roles;
    private boolean isEmailVerified;
    private String message;

    public AuthResponse(String token, Long id, String username, String email, Set<String> roles, boolean isEmailVerified) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.isEmailVerified = isEmailVerified;
    }

    public AuthResponse(String message) {
        this.message = message;
    }
}
