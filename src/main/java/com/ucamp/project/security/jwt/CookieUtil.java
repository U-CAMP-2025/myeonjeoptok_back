package com.ucamp.project.security.jwt;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class CookieUtil {
    private final AppProps props;
    public void setRefreshCookie(HttpServletResponse res, String token, int maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from(props.getCookie().getRefresh().getName(), token)
                .httpOnly(true).secure(props.getCookie().getRefresh().isSecure())
                .sameSite(props.getCookie().getRefresh().getSameSite())
                .domain(props.getCookie().getRefresh().getDomain())
                .path(props.getCookie().getRefresh().getPath())
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .build();
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
    public void clearRefreshCookie(HttpServletResponse res) {
        setRefreshCookie(res, "", 0);
    }
}