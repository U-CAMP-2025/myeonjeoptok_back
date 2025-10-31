package com.ucamp.project.controller;

import com.ucamp.project.auth.security.JwtTokenProvider;
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
            String at = jwt.access(u.getUserId());
            String rt = jwt.refresh(u.getUserId());
            u.setRefreshToken(rt);
            users.save(u);

            String nickname = URLEncoder.encode(u.getNickname(), StandardCharsets.UTF_8);
            String profileImageUrl = u.getUsersProfileImageUrl() != null
                    ? URLEncoder.encode(u.getUsersProfileImageUrl(), StandardCharsets.UTF_8)
                    : "";

            // 기존 유저: 프론트 홈으로 토큰 전달 리다이렉트
            String redirectUrl = String.format(
                    "%s/login/bridge?accessToken=%s&refreshToken=%s&nickname=%s&profileImageUrl=%s",
                    clientOrigin, at, rt, nickname, profileImageUrl
            );
            return ResponseEntity.status(302).location(URI.create(redirectUrl)).build();
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

        if (kakaoId == null || email == null) {
            // 카카오 세션이 만료된 경우 400 에러 응답
            return ResponseEntity.badRequest().body(Map.of("error", "카카오 세션이 만료되었습니다."));
        }

        // 신규 유저 생성
        User newUser = User.builder()
                .kakaoId(kakaoId)
                .email(email)
                .nickname(nickname)
                .jobId(jobId)
                .usersProfileImageUrl(profileImage)
                .status("ACTIVE")
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();
        users.save(newUser);

        // 토큰 발급
        String accessToken = jwt.access(newUser.getUserId());
        String refreshToken = jwt.refresh(newUser.getUserId());
        newUser.setRefreshToken(refreshToken);
        users.save(newUser);
        // 세션 정리
        session.invalidate();

        // 응답 데이터 구성
        Map<String, Object> responseBody = Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "nickname", newUser.getNickname(),
                "profileImageUrl", newUser.getUsersProfileImageUrl()
        );

        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/refresh")
    public Map<String,String> refresh(@RequestBody Map<String,String> b){
        String rt = b.get("refreshToken");
        if (!jwt.valid(rt)) throw new IllegalArgumentException("Invalid refresh token");
        Long uid = jwt.uid(rt);
        User u = users.findById(uid).orElseThrow();
        if (!rt.equals(u.getRefreshToken())) throw new IllegalStateException("Refresh token mismatch");

        String at = jwt.access(uid);
        String newRt = jwt.refresh(uid);
        u.setRefreshToken(newRt);
        users.save(u);
        return Map.of("accessToken", at, "refreshToken", newRt);
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
        System.out.println("닉네임: " + n);
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
}