package com.ucamp.project.service;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class OcrService {

    private final BizVerificationService bizVerificationService;
    private final ImageAnnotatorClient visionClient;

    public Map<String, Object> detectText(String imageUrl) throws IOException {
        Map<String, Object> responseMap = new HashMap<>();

        // (1) URL 이미지 로드
        URL url = new URL(imageUrl);
        byte[] data;
        try (InputStream in = url.openStream()) {
            data = in.readAllBytes();
        }

        // (2) Vision API 호출
        Image image = Image.newBuilder().setContent(ByteString.copyFrom(data)).build();
        Feature feature = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(feature)
                .setImage(image)
                .build();

        BatchAnnotateImagesResponse response =
                visionClient.batchAnnotateImages(Collections.singletonList(request));
        AnnotateImageResponse res = response.getResponses(0);

        if (res.hasError()) {
            responseMap.put("error", res.getError().getMessage());
            return responseMap;
        }

        String text = res.getFullTextAnnotation().getText();
        responseMap.put("text", text);
        log.info("OCR 인식 텍스트:\n{}", text);

        // (3) 사업자등록번호 추출
        String cleaned = text.replaceAll("\\s+", " ");
        Pattern pattern = Pattern.compile("사업자.{0,5}(\\d{3}\\D*\\d{2}\\D*\\d{5})");
        Matcher matcher = pattern.matcher(cleaned);

        if (matcher.find()) {
            String bizNum = matcher.group(1).replaceAll("\\D", "");
            log.info("추출된 사업자번호: {}", bizNum);
            responseMap.put("bizNum", bizNum);

            // (4) 검증 API 호출
            Map<String, Object> verfRes = bizVerificationService.verifyBusinessNumber(bizNum);
            responseMap.put("verfRes", verfRes);
        } else {
            responseMap.put("bizNum", "NOT_FOUND");
        }

        return responseMap;
    }

}

