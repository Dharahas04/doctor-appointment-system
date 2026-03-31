package com.app.doctorappointment.security;

import com.app.doctorappointment.model.User;
import com.app.doctorappointment.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthTokenService authTokenService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(AuthTokenService authTokenService, UserRepository userRepository) {
        this.authTokenService = authTokenService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            CurrentUser tokenUser = authTokenService.parseToken(token);

            if (tokenUser != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                userRepository.findById(tokenUser.userId())
                        .filter(user -> Boolean.TRUE.equals(user.getIsActive()))
                        .ifPresent(user -> authenticate(user, tokenUser));
            }
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(User user, CurrentUser tokenUser) {
        CurrentUser principal = new CurrentUser(user.getUserId(), user.getEmail(), user.getRole());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + tokenUser.role().name().toUpperCase())));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
