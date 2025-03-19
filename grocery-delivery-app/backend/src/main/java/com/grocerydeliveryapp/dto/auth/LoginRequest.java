package com.grocerydeliveryapp.dto.auth;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LoginRequest {
    @NotBlank(message = "Username/Email is required")
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    private String password;
}
