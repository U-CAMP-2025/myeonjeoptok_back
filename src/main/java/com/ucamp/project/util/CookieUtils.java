package com.ucamp.project.util;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

public class CookieUtils {
    public static final String RT_COOKIE = "RT";

    // refreshToken 심기
    public static ResponseCookie refreshCookie(String token, boolean https, String path) {
        return ResponseCookie.from(RT_COOKIE, token)
                .httpOnly(true)
                .secure(https)
                .sameSite(https ? "None" : "Lax")
                .path(path)
                .maxAge(Duration.ofDays(14))
                .build();
    }

    // 로그아웃/무효화
    public static ResponseCookie clearRefreshCookie(boolean https, String path) {
        return ResponseCookie.from("RT_COOKIE", "")
                .httpOnly(true)
                .secure(https)
                .sameSite(https ? "None": "Lax")
                .path(path)
                .maxAge(0)
                .build();
    }
}
