package com.ucamp.project.auth.security;

import com.ucamp.project.model.User;
import com.ucamp.project.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtTokenProvider {
    private final Key key;
    private final long accessMs, refreshMs;

    @Autowired
    private UserRepository userRepository;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") long accessMs,
            @Value("${jwt.refresh-expiration}") long refreshMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); // 32B+
        this.accessMs = accessMs;
        this.refreshMs = refreshMs;
    }

    public String access(User u)  { return acctoken(u); }
    public String refresh(User u) { return reftoken(u); }

    private String acctoken(User u) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(u.getUserId().toString())
                .setIssuedAt(now)
                .claim("role", "ROLE_"+u.getRole())
                .setExpiration(new Date(now.getTime() + accessMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private String reftoken(User u) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(u.getUserId().toString())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshMs))
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

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        if(claims.get("role") == null) {
            throw new RuntimeException("권한이 없는 토큰입니다.");
        }

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("role").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(userRepository.findById(this.uid(token)).orElseThrow(() -> new RuntimeException("없는 유저")),null,authorities);
    }

    public Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
