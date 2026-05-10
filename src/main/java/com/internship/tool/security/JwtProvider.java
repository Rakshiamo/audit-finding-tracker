package com.internship.tool.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.jwt.expiration}")
    private long jwtExpiration;

    private SecretKey getSigningKey() {

        if (jwtSecret == null || jwtSecret.isEmpty()) {

            throw new IllegalStateException(
                    "JWT secret is not configured. " +
                    "Set 'security.jwt.secret' property."
            );
        }

        if (jwtSecret.length() < 32) {

            log.warn(
                    "JWT secret is less than 32 characters " +
                    "(length: {}). This is insecure for HS256.",
                    jwtSecret.length()
            );
        }

        try {

            return Keys.hmacShaKeyFor(jwtSecret.getBytes());

        } catch (Exception e) {

            log.error(
                    "Failed to create signing key from JWT secret",
                    e
            );

            throw new RuntimeException(
                    "Failed to initialize JWT signing key",
                    e
            );
        }
    }

    // Generate token without role
    public String generateToken(String username) {

        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis() + jwtExpiration
                        )
                )
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Generate token WITH role
    // IMPORTANT:
    // Pass ADMIN not ROLE_ADMIN
    public String generateToken(String username, String role) {

        String normalizedRole = role;

        // Remove ROLE_ prefix if already present
        if (normalizedRole != null &&
            normalizedRole.startsWith("ROLE_")) {

            normalizedRole = normalizedRole.substring(5);
        }

        return Jwts.builder()
                .subject(username)
                .claim("role", normalizedRole)
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis() + jwtExpiration
                        )
                )
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {

        try {

            return getClaims(token).getSubject();

        } catch (Exception e) {

            log.error(
                    "Error extracting username from token: {}",
                    e.getMessage()
            );

            return null;
        }
    }

    public String getRoleFromToken(String token) {

        try {

            return getClaims(token)
                    .get("role", String.class);

        } catch (Exception e) {

            log.error(
                    "Error extracting role from token: {}",
                    e.getMessage()
            );

            return null;
        }
    }

    public boolean validateToken(String token) {

        try {

            getClaims(token);

            return true;

        } catch (Exception e) {

            log.error(
                    "JWT validation failed: {}",
                    e.getMessage()
            );

            return false;
        }
    }

    private Claims getClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}