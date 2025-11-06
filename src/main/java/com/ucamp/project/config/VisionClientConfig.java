package com.ucamp.project.config;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Configuration
public class VisionClientConfig {

    @Value("${vision.projectId}")
    private String PROJECT_ID;

    @Value("${vision.secretId}")
    private String SECRET_ID;

    @Bean
    public ImageAnnotatorClient imageAnnotatorClient() throws IOException {
        // 1️⃣ 부트스트랩 자격증명 확보
        GoogleCredentials bootstrapCreds = GoogleCredentials.getApplicationDefault();

        // 2️⃣ Secret Manager 클라이언트에 명시적으로 Credential 설정
        SecretManagerServiceSettings smSettings = SecretManagerServiceSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(bootstrapCreds))
                .build();

        // 3️⃣ Secret Manager 클라이언트 생성 (⚠️ smSettings 꼭 넣을 것!)
        try (SecretManagerServiceClient secretClient = SecretManagerServiceClient.create(smSettings)) {

            // 4️⃣ Secret 이름 지정 (latest 버전 사용)
            SecretVersionName secretVersionName = SecretVersionName.of(PROJECT_ID, SECRET_ID, "latest");

            // 5️⃣ Secret 값 읽기
            AccessSecretVersionResponse response = secretClient.accessSecretVersion(secretVersionName);
            String jsonKey = response.getPayload().getData().toStringUtf8();

            // 6️⃣ JSON 문자열을 GoogleCredentials 객체로 변환
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(jsonKey.getBytes())
            );

            // 7️⃣ Credentials를 Vision API 설정에 주입
            ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();

            // 8️⃣ Bean으로 반환
            return ImageAnnotatorClient.create(settings);
        }
    }
}
