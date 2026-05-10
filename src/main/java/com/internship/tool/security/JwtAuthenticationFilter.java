package com.internship.tool.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    // Skip Swagger & public endpoints
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getRequestURI();

        return path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/swagger-resources") ||
               path.startsWith("/webjars") ||
               path.startsWith("/auth") ||
               path.startsWith("/api/auth") ||
               path.startsWith("/actuator") ||
               path.equals("/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {

            String header = request.getHeader("Authorization");

            if (header != null && header.startsWith("Bearer ")) {

                String token = header.substring(7);

                if (jwtProvider.validateToken(token)) {

                    String username =
                            jwtProvider.getUsernameFromToken(token);

                    String role =
                            jwtProvider.getRoleFromToken(token);

                    List<SimpleGrantedAuthority> authorities =
                            new ArrayList<>();

                    if (role != null && !role.isBlank()) {

                        String normalizedRole = role.trim();

                        // Ensure ROLE_ prefix exists
                        if (!normalizedRole.startsWith("ROLE_")) {
                            normalizedRole = "ROLE_" + normalizedRole;
                        }

                        authorities.add(
                                new SimpleGrantedAuthority(normalizedRole)
                        );

                    } else {

                        authorities.add(
                                new SimpleGrantedAuthority("ROLE_USER")
                        );
                    }

                    System.out.println("Authorities: " + authorities);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    authorities
                            );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }
            }

        } catch (Exception e) {

            log.error("JWT Authentication failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}