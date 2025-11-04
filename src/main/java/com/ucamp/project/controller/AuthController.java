package com.ucamp.project.controller;

import com.ucamp.project.auth.security.JwtTokenProvider;
import com.ucamp.project.model.Job;
import com.ucamp.project.model.User;
import com.ucamp.project.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import com.ucamp.project.util.CookieUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository users;
    private final JwtTokenProvider jwt;

    @Value("${kakao.client-id}")     String clientId;
    @Value("${kakao.client-secret}") String clientSecret;
    @Value("${kakao.redirect-uri}")  String redirectUri;
    @Value("${client.origin:http://localhost:3000}") String clientOrigin;
    @Value("${kakao.admin-key:}") String adminKey;

    @Value("${app.https:false}") boolean https; // 운영환경 true
    @Value("${app.cookie-path:/}") String cookiePath;

    // 1. 인가 코드 요청
    @GetMapping("/kakao/login")
    public ResponseEntity<Void> login(HttpServletRequest req) {
        String state = UUID.randomUUID().toString();
        req.getSession(true).setAttribute("K_STATE", state);

        String url = UriComponentsBuilder.fromHttpUrl("https://kauth.kakao.com/oauth/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", "account_email,profile_image")
                .queryParam("state", state)
                .build(true).toUriString();
        return ResponseEntity.status(302).location(URI.create(url)).build();
    }

    @GetMapping("/kakao/callback")
    public ResponseEntity<Void> callback(@RequestParam String code,
                                         @RequestParam(required = false) String state,
                                         HttpServletRequest req) {
        var session = req.getSession(false);
        var saved = (session == null) ? null : session.getAttribute("K_STATE");
        if (saved == null || state == null || !state.equals(saved.toString()))
            throw new IllegalStateException("Invalid OAuth state");

        WebClient wc = WebClient.create();

        Map token = wc.post().uri("https://kauth.kakao.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type","authorization_code")
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("redirect_uri", redirectUri)
                        .with("code", code))
                .retrieve().bodyToMono(Map.class).block();

        String kakaoAccess = (String) token.get("access_token");

        Map me = wc.get().uri("https://kapi.kakao.com/v2/user/me")
                .headers(h -> h.setBearerAuth(kakaoAccess))
                .retrieve().bodyToMono(Map.class).block();

        String kakaoId = String.valueOf(me.get("id"));
        Map account = (Map) me.get("kakao_account");
        String email = (String) account.get("email");
        String profile = (String) ((Map) account.get("profile")).get("profile_image_url");

        Optional<User> found = users.findByKakaoId(kakaoId);
        if (found.isPresent()) {
            User u = found.get();

            // 재가입 플로우
            // 세션에 카카오 정보 적재 후 /signup
            if ("DISABLED".equalsIgnoreCase(u.getStatus())) {
                session.setAttribute("P_KAKAO_ID", kakaoId);
                session.setAttribute("P_EMAIL", email);
                session.setAttribute("P_PROFILE", profile);

                String redirectUrl = clientOrigin + "/signup";
                return ResponseEntity.status(302).location(URI.create(redirectUrl)).build();
            }

            // 정상 사용자
            String at = jwt.access(u);
            String rt = jwt.refresh(u);
            u.setRefreshToken(rt);
            users.save(u);

            var rtCookie = CookieUtils.refreshCookie(rt, https, cookiePath);

            String nickname = URLEncoder.encode(u.getNickname(), StandardCharsets.UTF_8);
            String profileImageUrl = u.getUsersProfileImageUrl() != null
                    ? URLEncoder.encode(u.getUsersProfileImageUrl(), StandardCharsets.UTF_8)
                    : "";
            // 로그인 후 다음으로 이동하고싶은 페이지 지정
            String nextPage = "/myqa";
            // 기존 유저: 프론트 홈으로 토큰 전달 리다이렉트
            String redirectUrl = String.format(
                    "%s/login/bridge?accessToken=%s&refreshToken=%s&nickname=%s&profileImageUrl=%s&next=%s",
                    clientOrigin, at, rt, nickname, profileImageUrl, URLEncoder.encode(nextPage, StandardCharsets.UTF_8)
            );

            return ResponseEntity.status(302)
                    .header("Set-Cookie", rtCookie.toString())
                    .location(URI.create(redirectUrl))
                    .build();
        }

        // 신규: 세션에 저장 후 /register 페이지로 리다이렉트
        session.setAttribute("P_KAKAO_ID", kakaoId);
        session.setAttribute("P_EMAIL", email);
        session.setAttribute("P_PROFILE", profile);

        String redirectUrl = clientOrigin + "/signup";
        return ResponseEntity.status(302).location(URI.create(redirectUrl)).build();
    }


    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody Map<String, String> body, HttpSession session) {
        String nickname = body.get("nickname");
        Long jobId = Long.valueOf(body.get("jobId"));
        String kakaoId = (String) session.getAttribute("P_KAKAO_ID");
        String email = (String) session.getAttribute("P_EMAIL");
        String profileImage = (String) session.getAttribute("P_PROFILE");

        if (kakaoId == null) {
            // 카카오 세션이 만료된 경우 400 에러 응답
            return ResponseEntity.badRequest().body(Map.of("error", "카카오 세션이 만료되었습니다."));
        }

        var existing = users.findByKakaoId(kakaoId);
        User u;
        if (existing.isPresent()) {
            // 재가입
            u = existing.get();
            u.setNickname(nickname);
            u.setJobId(jobId);
            u.setJob(Job.builder().jobId(jobId).build());
            u.setUsersProfileImageUrl(profileImage);
            u.setRole("USER");
            u.setStatus("ACTIVE");
            u.setEmail(email);
            u.setCreatedAt(u.getCreatedAt() == null ? LocalDateTime.now() : u.getCreatedAt());
            users.save(u);
        } else {
            // 최초가입
            u = users.save(User.builder()
                            .kakaoId(kakaoId)
                            .email(email)
                            .nickname(nickname)
                            .jobId(jobId)
                            .job(Job.builder().jobId(jobId).build())
                            .usersProfileImageUrl(profileImage)
                            .status("ACTIVE")
                            .role("USER")
                            .createdAt(LocalDateTime.now())
                    .build());
        }

        // 토큰 발급
        String accessToken = jwt.access(u);
        String refreshToken = jwt.refresh(u);
        u.setRefreshToken(refreshToken);
        users.save(u);
        // 세션 정리
        session.invalidate();

        var rtCookie = CookieUtils.refreshCookie(refreshToken, https, cookiePath);

        return ResponseEntity.ok()
                .header("Set-Cookie", rtCookie.toString())
                .body(Map.of(
                        "accessToken", accessToken,
                        "nickname", u.getNickname(),
                        "profileImageUrl", u.getUsersProfileImageUrl()
                ));
    }

    @PostMapping("/refresh")
    public Map<String,String> refresh(@CookieValue(value = CookieUtils.RT_COOKIE, required = false) String rt){
        System.out.println("-----------Token refresh");

        if (rt == null || !jwt.valid(rt)) throw new IllegalArgumentException("Invalid refresh token");

        Long uid = jwt.uid(rt);
        User u = users.findById(uid).orElseThrow();

        if (!rt.equals(u.getRefreshToken())) throw new IllegalStateException("Refresh token mismatch");

        // EASIEST HOTFIX: do not rotate refresh token here to avoid race-induced mismatches.
        // Just mint a new access token and return it. Keep the existing refresh token as-is.
        String at = jwt.access(u);
        return Map.of("accessToken", at);
    }

    @GetMapping("/nickname/check")
    public ResponseEntity<Map<String, Object>> checkNickname(@RequestParam String nickname, @RequestParam(required = false) Long excludeUserId) {
        String n = (nickname == null ? "": java.net.URLDecoder.decode(
                nickname, java.nio.charset.StandardCharsets.UTF_8
        )).trim();

        // 형식 검증
        if (n.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "available", false,
                    "reason", "EMPTY",
                    "message", "닉네임을 입력하세요."
            ));
        }

        if (!n.matches("^[A-Za-z0-9가-힣_]{2,10}$")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "available", false,
                    "reason", "INVALID_FORMAT",
                    "message", "닉네임은 2~10자의 한글/영문/숫자/밑줄만 사용할 수 있습니다."
            ));
        }

        // 중복 여부 체크
        boolean exists;
        if (excludeUserId == null) {
            exists = users.existsByNicknameIgnoreCase(n);
        } else {
            var found = users.findByNicknameIgnoreCase(n);
            exists = found.isPresent() && !found.get().getUserId().equals(excludeUserId);
        }

        return ResponseEntity.ok(Map.of(
                "available", !exists,
                "normalized", n
        ));
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        // 1) 우리 서비스의 refreshToken 쿠키 제거
        var clear = CookieUtils.clearRefreshCookie(https, cookiePath);

        // 2) 우리 DB의 refreshToken 무효화 및 Kakao 사용자 로그아웃 (best-effort)
        try {
            String bearer = (authorization != null && authorization.startsWith("Bearer ")) ? authorization.substring(7) : null;
            if (bearer != null && jwt.valid(bearer)) {
                Long uid = jwt.uid(bearer);
                users.findById(uid).ifPresent(u -> {
                    // DB 저장 리프레시 토큰 제거
                    u.setRefreshToken(null);
                    users.save(u);

                    // Kakao Admin Key가 설정된 경우 카카오 토큰 무효화 호출
                    if (u.getKakaoId() != null && adminKey != null && !adminKey.isBlank()) {
                        try {
                            WebClient.create()
                                    .post()
                                    .uri("https://kapi.kakao.com/v1/user/logout")
                                    .header("Authorization", "KakaoAK " + adminKey)
                                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                    .body(BodyInserters.fromFormData("target_id_type", "user_id")
                                            .with("target_id", u.getKakaoId()))
                                    .retrieve()
                                    .bodyToMono(Map.class)
                                    .onErrorResume(e -> reactor.core.publisher.Mono.empty())
                                    .block();
                        } catch (Exception ignore) {
                            // swallow errors
                        }
                    }
                });
            }
        } catch (Exception ignore) {
            // swallow errors
        }

        // 3) 브라우저 카카오 계정 세션까지 종료하도록 카카오 로그아웃으로 302 리다이렉트
        String url = UriComponentsBuilder.fromHttpUrl("https://kauth.kakao.com/oauth/logout")
                .queryParam("client_id", clientId)
                .queryParam("logout_redirect_uri", clientOrigin + "/logout/complete")
                .build(true).toUriString();

        return ResponseEntity.status(302)
                .header("Set-Cookie", clear.toString())
                .location(URI.create(url))
                .build();
    }


    @DeleteMapping("/withdraw")
    public ResponseEntity<Map<String, Object>> withdraw(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        String bearer = (authorization != null && authorization.startsWith("Bearer ")) ? authorization.substring(7) : null;
        if (bearer == null || !jwt.valid(bearer)) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false, "message", "Invalid or missing access token"
            ));
        }

        Long uid = jwt.uid(bearer);
        User u = users.findById(uid).orElse(null);
        if (u == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "success", false, "message", "User not found"
            ));
        }

        // 우리 서비스 계정 비활성화
        u.setStatus("DISABLED");
        u.setRefreshToken(null);

        String suffix = "_" + uid;
        if (u.getNickname() != null) u.setNickname("deleted" + suffix);
        if (u.getEmail() != null) u.setEmail("deleted" + suffix + "@example.invalid");
        users.save(u);

        // 클라이언트 refresh 쿠키 제거
        var clear = CookieUtils.clearRefreshCookie(https, cookiePath);

        // 카카오 연결 끊기
        try {
            if (u.getKakaoId() != null && adminKey != null && !adminKey.isBlank()) {
                WebClient.create()
                        .post()
                        .uri("https://kapi.kakao.com/v1/user/unlink")
                        .header("Authorization", "KakaoAK " + adminKey)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(BodyInserters.fromFormData("target_id_type", "user_id")
                                .with("target_id", u.getKakaoId()))
                        .retrieve()
                        .bodyToMono(Map.class)
                        .onErrorResume(e -> reactor.core.publisher.Mono.empty())
                        .block();
            }
        } catch (Exception ignore) {}

        return ResponseEntity.ok()
                .header("Set-Cookie", clear.toString())
                .body(Map.of("success", true, "message", "Account has been withdrawn"));
    }

}