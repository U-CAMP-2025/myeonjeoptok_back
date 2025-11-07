package com.ucamp.project.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order // 우선순위는 기본값이면 충분 (중요: 체인 "전에" wrapper를 씌워야 함)
public class PartitionedSessionCookieFilter extends OncePerRequestFilter {

    private static final String SET_COOKIE = "Set-Cookie";
    private static final String JSESSIONID_PREFIX = "JSESSIONID=";

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {

        HttpServletResponseWrapper wrapping = new HttpServletResponseWrapper(res) {

            // Set-Cookie 헤더를 추가/설정할 때마다 가로채서 보강
            @Override
            public void addHeader(String name, String value) {
                if (SET_COOKIE.equalsIgnoreCase(name)) {
                    super.addHeader(name, enhanceIfSessionCookie(value));
                } else {
                    super.addHeader(name, value);
                }
            }

            @Override
            public void setHeader(String name, String value) {
                if (SET_COOKIE.equalsIgnoreCase(name)) {
                    super.setHeader(name, enhanceIfSessionCookie(value));
                } else {
                    super.setHeader(name, value);
                }
            }

            // 서블릿 API의 addCookie로 들어오는 경우도 안전하게 처리
            @Override
            public void addCookie(Cookie cookie) {
                if ("JSESSIONID".equalsIgnoreCase(cookie.getName())) {
                    cookie.setHttpOnly(true);
                    cookie.setSecure(true); // Partitioned 전제
                    // SameSite/Partitioned는 Cookie 객체로는 못 붙임 → 수동으로 Set-Cookie 생성
                    String raw = "JSESSIONID=" + cookie.getValue()
                            + "; Path=" + (cookie.getPath() != null ? cookie.getPath() : "/")
                            + "; HttpOnly; Secure; SameSite=None; Partitioned";
                    // 기존 addCookie 대신 헤더로 직접 추가
                    super.addHeader(SET_COOKIE, raw);
                } else {
                    super.addCookie(cookie);
                }
            }

            private String enhanceIfSessionCookie(String setCookieValue) {
                if (setCookieValue == null) return null;
                // JSESSIONID 아닌 쿠키는 그대로 통과
                if (!setCookieValue.regionMatches(true, 0, JSESSIONID_PREFIX, 0, JSESSIONID_PREFIX.length())) {
                    return setCookieValue;
                }
                String v = setCookieValue;

                String lower = v.toLowerCase();
                // 누락된 속성만 보강 (중복 방지)
                if (!lower.contains("samesite"))   v += "; SameSite=None";
                if (!lower.contains("secure"))     v += "; Secure";
                if (!lower.contains("httponly"))   v += "; HttpOnly";
                if (!lower.contains("partitioned")) v += "; Partitioned";

                // Path 없으면 기본 Path=/
                if (!lower.contains("path="))      v += "; Path=/";

                return v;
            }
        };

        chain.doFilter(req, wrapping);
    }
}