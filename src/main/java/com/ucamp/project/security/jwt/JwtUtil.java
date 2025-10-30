package com.ucamp.project.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final AppProps props; // yaml 바인딩

    private Key key() {
        return Keys.hmacShaKeyFor(props.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long userId, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setIssuer(props.getJwt().getIssuer())
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(Duration.ofMinutes(props.getJwt().getAccessExpMinutes()))))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String jwt) {
        return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(jwt);
    }

    public Long getUserId(String jwt) {
        return Long.valueOf(parse(jwt).getBody().getSubject());
    }
}