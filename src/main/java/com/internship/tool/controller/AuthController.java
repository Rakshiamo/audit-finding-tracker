package com.internship.tool.controller;

import com.internship.tool.dto.AuthResponse;
import com.internship.tool.dto.LoginRequest;
import com.internship.tool.dto.RegisterRequest;
import com.internship.tool.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user identity management, including login, registration, and token rotation.")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Validates credentials and returns a JWT access token and refresh token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
            @ApiResponse(responseCode = "401", description = "Invalid username or password"),
            @ApiResponse(responseCode = "400", description = "Bad request - check payload format")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    @Operation(summary = "Register new account", description = "Creates a new user profile with default roles. Encrypts password automatically.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "User already exists or validation failed"),
            @ApiResponse(responseCode = "422", description = "Unprocessable entity - invalid data format")
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate tokens", description = "Issues a fresh JWT access token using a valid Refresh Token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens rotated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - token is expired or blacklisted"),
            @ApiResponse(responseCode = "400", description = "Required Authorization header is missing")
    })
    public ResponseEntity<AuthResponse> refresh(@RequestHeader("Authorization") String authHeader) {
        // Cleaning the Bearer prefix if present
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        return ResponseEntity.ok(authService.refresh(token));
    }
}
