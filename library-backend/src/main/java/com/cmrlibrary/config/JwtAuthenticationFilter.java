
package com.cmrlibrary.config;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cmrlibrary.entity.User;
import com.cmrlibrary.repository.UserRepository;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository) {

        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        try {
            Claims claims = jwtService.extractAllClaims(token);
            String email = claims.getSubject();

            if (email != null
                    && SecurityContextHolder.getContext()
                            .getAuthentication() == null) {

                User user = userRepository.findByEmail(email)
                        .orElse(null);

                if (user != null
                        && jwtService.isTokenValid(token, email)) {

                    // Always use the current database role.
                    String databaseRole = user.getRole();

                    String tokenRole =
                            claims.get("role", String.class);

                    // Reject tokens with an outdated or mismatched role.
                    if (databaseRole != null
                            && databaseRole.equalsIgnoreCase(tokenRole)) {

                        String authority =
                                "ROLE_" + databaseRole.toUpperCase();

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        email,
                                        null,
                                        List.of(
                                            new SimpleGrantedAuthority(
                                                    authority
                                            )
                                        )
                                );

                        SecurityContextHolder.getContext()
                                .setAuthentication(authentication);
                    }
                }
            }

        } catch (Exception exception) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
