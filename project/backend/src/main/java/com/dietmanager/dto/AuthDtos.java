package com.dietmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthDtos {
    public static class SignupRequest {
        @NotBlank
        public String fullName;
        @Email
        public String email;
        @NotBlank
        public String password;
    }

    public static class LoginRequest {
        @Email
        public String email;
        @NotBlank
        public String password;
    }

    public static class AuthResponse {
        public String userId;
        public String fullName;
        public String email;
        public String token;

        public AuthResponse(String userId, String fullName, String email, String token) {
            this.userId = userId;
            this.fullName = fullName;
            this.email = email;
            this.token = token;
        }
    }
}
