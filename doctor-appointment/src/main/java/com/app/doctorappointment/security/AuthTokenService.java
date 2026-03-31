package com.app.doctorappointment.security;

import com.app.doctorappointment.model.Role;
import com.app.doctorappointment.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AuthTokenService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String secret;
    private final long expirationMinutes;

    public AuthTokenService(@Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.expiration-minutes}") long expirationMinutes) {
        this.secret = secret;
        this.expirationMinutes = expirationMinutes;
    }

    public String createToken(User user) {
        try {
            String header = encodeJson(Map.of("alg", "HS256", "typ", "JWT"));

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", user.getUserId());
            payload.put("email", user.getEmail());
            payload.put("role", user.getRole().name());
            payload.put("exp", Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES).getEpochSecond());

            String body = encodeJson(payload);
            String signature = sign(header + "." + body);
            return header + "." + body + "." + signature;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create auth token.", ex);
        }
    }

    public CurrentUser parseToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            String expectedSignature = sign(parts[0] + "." + parts[1]);
            if (!expectedSignature.equals(parts[2])) {
                return null;
            }

            Map<String, Object> payload = decodeJson(parts[1]);
            long expiration = ((Number) payload.get("exp")).longValue();
            if (Instant.now().getEpochSecond() > expiration) {
                return null;
            }

            Long userId = ((Number) payload.get("sub")).longValue();
            String email = String.valueOf(payload.get("email"));
            Role role = Role.valueOf(String.valueOf(payload.get("role")));
            return new CurrentUser(userId, email, role);
        } catch (Exception ex) {
            return null;
        }
    }

    private String encodeJson(Map<String, ?> payload) throws Exception {
        String json = objectMapper.writeValueAsString(payload);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    private Map<String, Object> decodeJson(String encodedJson) throws Exception {
        byte[] decodedBytes = Base64.getUrlDecoder().decode(encodedJson);
        return objectMapper.readValue(decodedBytes, MAP_TYPE);
    }

    private String sign(String value) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(hmac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }
}
