package com.ucamp.project.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")

public class AppProps {

    // yml의 'app.frontend-origin'이 여기에 매핑됩니다.
    private String frontendOrigin;

    // yml의 'app.jwt' 하위 속성들이 매핑됩니다.
    private final Jwt jwt = new Jwt();

    // yml의 'app.cookie' 하위 속성들이 매핑됩니다.
    private final Cookie cookie = new Cookie();

    // yml의 'app.kakao' 하위 속성들이 매핑됩니다.
    private final Kakao kakao = new Kakao();

    @Getter
    @Setter
    public static class Jwt {
        private String issuer;
        // yml의 access-exp-minutes -> accessExpMinutes (카멜 케이스)
        private long accessExpMinutes;
        private long refreshExpDays;
        private String secret;
    }

    @Getter
    @Setter
    public static class Cookie {
        private final Refresh refresh = new Refresh();

        @Getter
        @Setter
        public static class Refresh {
            private String name;
            private String domain;
            private String path;
            private boolean secure;
            private String sameSite;
        }
    }

    @Getter
    @Setter
    public static class Kakao {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String tokenUri;
        private String userinfoUri;
    }
}