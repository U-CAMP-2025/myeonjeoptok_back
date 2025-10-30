package com.ucamp.project.service;

import com.ucamp.project.model.KakaoProfile;
import com.ucamp.project.security.jwt.AppProps;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService {
    private final AppProps props;
    private final WebClient web = WebClient.builder().build();

    public String buildAuthorizeUrl() {
        String state = UUID.randomUUID().toString();
        // state는 서버 세션/캐시에 저장해두고 callback에서 검증하도록 구현
        return UriComponentsBuilder.fromUriString("https://kauth.kakao.com/oauth/authorize")
                .queryParam("client_id", props.getKakao().getClientId())
                .queryParam("redirect_uri", props.getKakao().getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("state", state)
                .build().toString();
    }

    public KakaoProfile exchangeAndGetProfile(String code, String state) {
        // state 검증 로직 생략(저장/비교)
        Map<String, Object> token = web.post()
                .uri(props.getKakao().getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type","authorization_code")
                        .with("client_id", props.getKakao().getClientId())
                        .with("client_secret", props.getKakao().getClientSecret())
                        .with("redirect_uri", props.getKakao().getRedirectUri())
                        .with("code", code))
                .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String,Object>>(){}).block();

        String accessToken = (String) token.get("access_token");

        Map<String, Object> profile = web.get()
                .uri(props.getKakao().getUserinfoUri())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String,Object>>(){}).block();

        // 필요한 값 파싱(안전하게 map 탐색)
        Map<String,Object> kakaoAccount = (Map<String,Object>) profile.get("kakao_account");
        Map<String,Object> p = (Map<String,Object>) kakaoAccount.get("profile");
        KakaoProfile kp = new KakaoProfile();
        kp.setId(((Number)profile.get("id")).longValue());
        kp.setEmail((String) kakaoAccount.get("email")); // 동의 안하면 null
        if (p != null) kp.setProfileImageUrl((String) p.get("profile_image_url"));
        return kp;
    }

    public String issueRegistrationToken(KakaoProfile p) {
        // 간단히 JWS로 만들어도 되고, registration_token 테이블에 저장해도 됨
        // 여기서는 간단화를 위해 JWS(5분 만료)라고 가정
        Instant now = Instant.now();
        return Jwts.builder()
                .setIssuer("reg-token")
                .claim("kid", p.getId())
                .claim("email", p.getEmail())
                .claim("pic", p.getProfileImageUrl())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(Duration.ofMinutes(5))))
                .signWith(Keys.hmacShaKeyFor("reg-secret-32bytes-minimum-reg-secret".getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    public KakaoProfile verifyRegistrationToken(String token) {
        try {
            Claims c = Jwts.parserBuilder()
                    .setSigningKey("reg-secret-32bytes-minimum-reg-secret".getBytes())
                    .build().parseClaimsJws(token).getBody();
            KakaoProfile p = new KakaoProfile();
            p.setId(((Number)c.get("kid")).longValue());
            p.setEmail((String)c.get("email"));
            p.setProfileImageUrl((String)c.get("pic"));
            return p;
        } catch (JwtException e) { return null; }
    }
}