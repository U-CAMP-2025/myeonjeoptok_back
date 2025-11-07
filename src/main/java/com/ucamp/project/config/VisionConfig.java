package com.ucamp.project.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Configuration
public class VisionConfig {
    // ✅ 자네의 실제 GCP 프로젝트 ID와 Secret 이름으로 교체
    @Value("${vision.projectId}")
    private String PROJECT_ID;
    @Value("${vision.secretId}")
    private String SECRET_ID;

    @Bean
    public ImageAnnotatorClient imageAnnotatorClient() throws IOException {
        try (SecretManagerServiceClient secretClient = SecretManagerServiceClient.create()) {

            // 1️⃣ Secret Manager에서 JSON 키 불러오기
            SecretVersionName secretVersionName = SecretVersionName.of(PROJECT_ID, SECRET_ID, "latest");
            AccessSecretVersionResponse response = secretClient.accessSecretVersion(secretVersionName);
            String jsonKey = response.getPayload().getData().toStringUtf8();

            // 2️⃣ JSON을 GoogleCredentials로 변환
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(jsonKey.getBytes())
            ).createScoped("https://www.googleapis.com/auth/cloud-platform");

            // 3️⃣ Credentials를 Vision API 설정에 주입
            ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                    .setCredentialsProvider(() -> credentials)
                    .build();

            // 4️⃣ Bean 반환
            return ImageAnnotatorClient.create(settings);
        }
    }
}
