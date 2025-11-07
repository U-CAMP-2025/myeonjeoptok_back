package com.ucamp.project;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UcampProjectServerApplication {

    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.load();

        System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
        System.setProperty("KAKAO_CLIENT_ID", dotenv.get("KAKAO_CLIENT_ID"));
        System.setProperty("KAKAO_CLIENT_SECRET", dotenv.get("KAKAO_CLIENT_SECRET"));
        System.setProperty("KAKAO_REDIRECT_URI", dotenv.get("KAKAO_REDIRECT_URI"));
        System.setProperty("KAKAO_ADMIN_KEY", dotenv.get("KAKAO_ADMIN_KEY"));
        System.setProperty("OPENAI_API_KEY", dotenv.get("OPENAI_API_KEY"));
        System.setProperty("OPENAI_STT_MODEL", dotenv.get("OPENAI_STT_MODEL"));
        System.setProperty("ORACLE_URL", dotenv.get("ORACLE_URL"));
        System.setProperty("ORACLE_USER", dotenv.get("ORACLE_USER"));
        System.setProperty("ORACLE_PASSWORD", dotenv.get("ORACLE_PASSWORD"));
        System.setProperty("ODCLOUD_API_KEY", dotenv.get("ODCLOUD_API_KEY"));
        System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", dotenv.get("GOOGLE_APPLICATION_CREDENTIALS"));

        SpringApplication.run(UcampProjectServerApplication.class, args);
    }

}
