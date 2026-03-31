package com.app.doctorappointment.config;

import com.app.doctorappointment.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.CorsUtils;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/specialties", "/api/doctors", "/api/slots").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/specialties", "/api/doctors", "/api/slots").hasRole("ADMIN")
                        .requestMatchers("/api/reports/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/appointments/reserve", "/api/appointments/confirm").hasRole("PATIENT")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/appointments/reserve").hasRole("PATIENT")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/appointments").hasRole("PATIENT")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/appointments/status").hasAnyRole("ADMIN", "DOCTOR", "PATIENT")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/appointments").authenticated()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("Username/password login is handled by /api/auth endpoints.");
        };
    }
}
