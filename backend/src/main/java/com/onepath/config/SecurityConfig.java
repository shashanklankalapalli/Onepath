package com.onepath.config;

import com.onepath.security.CustomUserDetailsService;
import com.onepath.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(CustomUserDetailsService userDetailsService, JwtAuthenticationFilter jwtAuthFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/colleges/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/services/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/services/*/evaluate").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/updates/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/service-centers/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/service-centres/**").permitAll()
                        .requestMatchers("/api/chat/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        // Admin / College Admin protected mutation endpoints
                        .requestMatchers(HttpMethod.POST, "/api/colleges/**").hasAnyRole("COLLEGE_ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/colleges/**").hasAnyRole("COLLEGE_ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/colleges/**").hasAnyRole("COLLEGE_ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/services/**").hasRole("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/services/**").hasRole("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/services/**").hasRole("SUPER_ADMIN")
                        // Authenticated user endpoints
                        .requestMatchers("/api/checklists/**").authenticated()
                        .requestMatchers("/api/documents/**").authenticated()
                        .requestMatchers("/api/users/**").authenticated()
                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "http://localhost:3000",
                "http://127.0.0.1:5173",
                "http://127.0.0.1:3000",
                "http://*:5173",
                "http://*:3000",
                "*"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Link", "X-Total-Count"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
