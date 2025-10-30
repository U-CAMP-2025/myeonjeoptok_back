package com.ucamp.project.controller;

import com.ucamp.project.model.KakaoProfile;
import com.ucamp.project.model.User;
import com.ucamp.project.repository.UserRepository;
import com.ucamp.project.security.jwt.CookieUtil;
import com.ucamp.project.security.jwt.JwtUtil;
import com.ucamp.project.service.KakaoOAuthService;
import com.ucamp.project.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final KakaoOAuthService kakao;
    private final UserRepository users;
    private final JwtUtil jwt;
    private final RefreshTokenService refreshSvc;
    private final CookieUtil cookieUtil;

    @GetMapping("/api/auth/kakao/login")
    public void login(HttpServletResponse res) throws IOException {
        String url = kakao.buildAuthorizeUrl(); // state 포함
        res.sendRedirect(url);
    }

    @GetMapping("/auth/kakao/callback")
    public ResponseEntity<?> callback(@RequestParam String code, @RequestParam String state,
                                      HttpServletRequest req, HttpServletResponse res) throws Exception {
        KakaoProfile profile = kakao.exchangeAndGetProfile(code, state);
        Optional<User> found = users.findByKakaoId(String.valueOf(profile.getId()));
        if (found.isPresent()) {
            return issueTokensAndReturn(found.get(), req, res);
        } else {
            // registration_token 쿠키 발급 (5분)
            String registrationToken = kakao.issueRegistrationToken(profile);
            ResponseCookie cookie = ResponseCookie.from("registration_token", registrationToken)
                    .httpOnly(true).secure(true).sameSite("Strict")
                    .path("/auth").maxAge(Duration.ofMinutes(5)).build();
            res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            // 프론트 가입 페이지로 리다이렉트
            URI to = URI.create("https://app.example.com/signup");
            return ResponseEntity.status(HttpStatus.FOUND).location(to).build();
        }
    }

    // 3) 회원가입 완료
    @PostMapping("/api/auth/signup")
    public ResponseEntity<?> complete(@RequestBody CompleteReq body,
                                      @CookieValue("registration_token") String regToken,
                                      HttpServletRequest req, HttpServletResponse res) throws Exception {
        KakaoProfile p = kakao.verifyRegistrationToken(regToken);
        if (p == null) return ResponseEntity.status(401).build();

        if (users.findByNickname(body.nickname()).isPresent())
            return ResponseEntity.status(409).body(Map.of("message","nickname exists"));

        User u = new User();
        u.setKakaoId(String.valueOf(p.getId()));
        u.setNickname(body.nickname());
        u.setEmail(p.getEmail()); // null 허용
        u.setUsersProfileImageUrl(p.getProfileImageUrl());
        u.setJobId(body.jobId());
        u.setStatus("ACTIVE");
        u.setRole("USER");
        u.setCreatedAt(LocalDateTime.now());
        users.save(u);

        // 가입 후 바로 토큰 발급
        return issueTokensAndReturn(u, req, res);
    }

    private ResponseEntity<?> issueTokensAndReturn(User u, HttpServletRequest req, HttpServletResponse res) throws Exception {
        String access = jwt.createAccessToken(u.getUserId(), u.getRole());
        String rawRefresh = refreshSvc.issueAndStore(u.getUserId(), req.getHeader("User-Agent"));
        // refresh는 HttpOnly 쿠키로
        cookieUtil.setRefreshCookie(res, rawRefresh, 60*60*24*7);
        return ResponseEntity.ok(Map.of("accessToken", access, "user", Map.of(
                "id", u.getUserId(), "nickname", u.getNickname(), "role", u.getRole()
        )));
    }

    // 4) 토큰 재발급(로테이션)
    @PostMapping("/api/auth/refresh")
    public ResponseEntity<?> refresh(@CookieValue(name="refresh_token", required=false) String raw,
                                     HttpServletRequest req, HttpServletResponse res) throws Exception {
        if (raw == null || raw.isBlank()) return ResponseEntity.status(401).build();
        Long userId = refreshSvc.verifyAndRotate(raw, req.getHeader("User-Agent"));
        User u = users.findById(userId).orElseThrow();
        String access = jwt.createAccessToken(userId, u.getRole());
        String newRefresh = refreshSvc.issueAndStore(userId, req.getHeader("User-Agent"));
        cookieUtil.setRefreshCookie(res, newRefresh, 60*60*24*7);
        return ResponseEntity.ok(Map.of("accessToken", access));
    }

    // 5) 로그아웃
    @PostMapping("/api/auth/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal Long userId, HttpServletResponse res) {
        if (userId != null) refreshSvc.revokeAll(userId);
        cookieUtil.clearRefreshCookie(res);
        return ResponseEntity.ok().build();
    }

    public record CompleteReq(String nickname, Long jobId) {}
}