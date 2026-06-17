package com.engine.taskmanagement.auth.service;

import com.engine.taskmanagement.auth.enums.Role;
import com.engine.taskmanagement.common.exception.UnauthorizedException;
import com.engine.taskmanagement.user.entity.User;
import com.engine.taskmanagement.user.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String TOKEN_TYPE = "JWT";
    private static final String ROLE_PREFIX = "ROLE_";

    private final String secret;
    private final long expirationSeconds;
    private final String issuer;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtService(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.expiration-seconds:3600}") long expirationSeconds,
            @Value("${app.security.jwt.issuer:rivet-api}") String issuer,
            UserRepository userRepository
    ) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes");
        }
        this.secret = secret;
        this.expirationSeconds = expirationSeconds;
        this.issuer = issuer;
        this.userRepository = userRepository;
    }

    public String generateToken(User user) {
        Role role = user.getRole() == null ? Role.USER : user.getRole();
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expirationSeconds);

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", TOKEN_TYPE);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("iss", issuer);
        payload.put("sub", user.getEmail());
        payload.put("userId", user.getId());
        payload.put("role", role.name());
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", expiresAt.getEpochSecond());

        String unsignedToken = base64UrlJson(header) + "." + base64UrlJson(payload);
        return unsignedToken + "." + sign(unsignedToken);
    }

    public Authentication toAuthentication(String token) {
        Map<String, Object> claims = parseAndValidate(token);
        String email = claimAsString(claims, "sub");
        User user = userRepository.findByEmail(email)
                .filter(existingUser -> existingUser.getDeletedAt() == null)
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));
        Role role = user.getRole() == null ? Role.USER : user.getRole();

        return new UsernamePasswordAuthenticationToken(
                email,
                null,
                List.of(new SimpleGrantedAuthority(ROLE_PREFIX + role.name()))
        );
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public Duration getRemainingValidity(String token) {
        Map<String, Object> claims = parseAndValidate(token);
        long expiresAt = claimAsLong(claims, "exp");
        long remainingSeconds = expiresAt - Instant.now().getEpochSecond();
        return Duration.ofSeconds(Math.max(remainingSeconds, 0));
    }

    private Map<String, Object> parseAndValidate(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new UnauthorizedException("Invalid token");
        }

        String unsignedToken = parts[0] + "." + parts[1];
        if (!constantTimeEquals(sign(unsignedToken), parts[2])) {
            throw new UnauthorizedException("Invalid token signature");
        }

        Map<String, Object> claims = decodeJson(parts[1]);
        if (!issuer.equals(claimAsString(claims, "iss"))) {
            throw new UnauthorizedException("Invalid token issuer");
        }

        long expiresAt = claimAsLong(claims, "exp");
        if (Instant.now().getEpochSecond() >= expiresAt) {
            throw new UnauthorizedException("Token expired");
        }

        return claims;
    }

    private String base64UrlJson(Map<String, Object> value) {
        try {
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (Exception ex) {
            throw new IllegalStateException("Could not serialize JWT content", ex);
        }
    }

    private Map<String, Object> decodeJson(String value) {
        try {
            byte[] json = Base64.getUrlDecoder().decode(value);
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception ex) {
            throw new UnauthorizedException("Invalid token payload");
        }
    }

    private String sign(String unsignedToken) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Could not sign JWT", ex);
        }
    }

    private boolean constantTimeEquals(String expected, String actual) {
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        byte[] actualBytes = actual.getBytes(StandardCharsets.UTF_8);
        return java.security.MessageDigest.isEqual(expectedBytes, actualBytes);
    }

    private String claimAsString(Map<String, Object> claims, String name) {
        Object value = claims.get(name);
        if (!(value instanceof String stringValue) || stringValue.isBlank()) {
            throw new UnauthorizedException("Invalid token claim: " + name);
        }
        return stringValue;
    }

    private long claimAsLong(Map<String, Object> claims, String name) {
        Object value = claims.get(name);
        if (value instanceof Number numberValue) {
            return numberValue.longValue();
        }
        throw new UnauthorizedException("Invalid token claim: " + name);
    }
}
