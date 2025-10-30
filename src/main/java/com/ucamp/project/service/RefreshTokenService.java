package com.ucamp.project.service;

import com.ucamp.project.model.RefreshToken;
import com.ucamp.project.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import com.ucamp.project.security.jwt.AppProps;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository repo;
    private final AppProps props;

    private String hash(String raw) throws NoSuchAlgorithmException {
        return Base64.getEncoder().encodeToString(
                MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8))
        );
    }

    public String issueAndStore(Long userId, String userAgent) throws Exception {
        String raw = UUID.randomUUID().toString() + "." + userId;
        String hashed = hash(raw);
        RefreshToken rt = new RefreshToken();
        rt.setUserId(userId);
        rt.setTokenHash(hashed);
        rt.setUserAgent(Optional.ofNullable(userAgent).orElse("NA"));
        rt.setCreatedAt(LocalDateTime.now());
        rt.setExpiresAt(LocalDateTime.now().plusDays(props.getJwt().getRefreshExpDays()));
        repo.save(rt);
        return raw; // 원문은 쿠키로 내려보냄
    }

    public Long verifyAndRotate(String raw, String userAgent) throws Exception {
        String hashed = hash(raw);
        RefreshToken found = repo.findByTokenHashAndRevokedAtIsNull(hashed)
                .orElseThrow(() -> new AccessDeniedException("invalid refresh"));

        if (found.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new AccessDeniedException("expired refresh");

        // 재사용 공격 방지: 기존 토큰 즉시 revoke
        found.setRevokedAt(LocalDateTime.now());
        repo.save(found);

        return found.getUserId();
    }

    public void revokeAll(Long userId) {
        repo.findByUserIdAndRevokedAtIsNull(userId).forEach(rt -> {
            rt.setRevokedAt(LocalDateTime.now());
            repo.save(rt);
        });
    }
}