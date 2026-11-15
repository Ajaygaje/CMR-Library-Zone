
package com.cmrlibrary.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .cors(cors -> cors.configurationSource(
                    corsConfigurationSource()))

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth

                // Public APIs
                .requestMatchers(
                    "/api/users/register",
                    "/api/users/login",
                    "/api/health"
                ).permitAll()

                // Allow browser preflight requests
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Books: all roles can view
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/books",
                    "/api/books/**"
                ).hasAnyRole("ADMIN", "LIBRARIAN", "STUDENT")

                // Books: Admin and Librarian can modify
                .requestMatchers("/api/books/**")
                .hasAnyRole("ADMIN", "LIBRARIAN")

                // Student: view own profile
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/students/profile"
                ).hasRole("STUDENT")

                // Student: update own phone number
                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/students/profile"
                ).hasRole("STUDENT")

                // Student management: Admin and Librarian
                .requestMatchers("/api/students/**")
                .hasAnyRole("ADMIN", "LIBRARIAN")

                // Student: view own borrowing history
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/issues/my-history",
                    "/api/issues/my-active"
                ).hasRole("STUDENT")

                // Student, Admin and Librarian: borrow books
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/issues/issue"
                ).hasAnyRole("STUDENT", "ADMIN", "LIBRARIAN")

                // Issue management: Admin and Librarian only
                .requestMatchers("/api/issues/**")
                .hasAnyRole("ADMIN", "LIBRARIAN")

                // Dashboard: Admin and Librarian
                .requestMatchers("/api/dashboard/**")
                .hasAnyRole("ADMIN", "LIBRARIAN")

                // Account creation: Admin only
                .requestMatchers("/api/users/create")
                .hasRole("ADMIN")

                // All other APIs require authentication
                .anyRequest().authenticated()
            )

            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("*"));

        configuration.setAllowedMethods(
            List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")
        );

        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
