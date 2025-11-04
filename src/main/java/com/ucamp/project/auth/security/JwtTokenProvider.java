package com.ucamp.project.auth.security;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final Key key;
    private final long accessMs, refreshMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") long accessMs,
            @Value("${jwt.refresh-expiration}") long refreshMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); // 32B+
        this.accessMs = accessMs;
        this.refreshMs = refreshMs;
    }

    public String access(Long uid)  { return token(uid, accessMs); }
    public String refresh(Long uid) { return token(uid, refreshMs); }

    private String token(Long uid, long ms) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(uid.toString())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ms))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean valid(String t){
        try { Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(t); return true; }
        catch (JwtException e){ return false; }
    }
    public Long uid(String t){
        return Long.valueOf(Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(t).getBody().getSubject());
    }
}
