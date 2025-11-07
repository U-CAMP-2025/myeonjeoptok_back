package com.ucamp.project.service;

import com.nimbusds.jose.util.Pair;
import com.ucamp.project.model.Certificate;
import com.ucamp.project.repository.CertRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;


@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {
    private final CertRepository certRepository;

    public Pair<String,Resource> getImg(String type, String id) {


        String url;
        Resource imageResource;
        String contentType;

        if("cert".equals(type)){
            url = "/opt/ucamp/uploads/"+type+"/" + id;
        } else {
            url = "/opt/ucamp/uploads/"+type+"/" + id + ".png";
        }

        try {
            Path path = Paths.get(url);
            imageResource = new UrlResource(Objects.requireNonNull(path).toUri());
            contentType = Files.probeContentType(path);
        } catch (IOException e) {
            throw new RuntimeException("이미지 처리 실패");
        }


        return Pair.of(contentType, imageResource);

    }

//    // 파일 저장 (DB insert X)
//    public String saveFile(String type, MultipartFile file) {
//        try {
//            Path dirPath = Paths.get("src/main/resources/static/image/" + type);
//            if (!Files.exists(dirPath)) {
//                Files.createDirectories(dirPath);
//            }
//
//            // UUID 기반 파일명 (중복 방지)
//            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
//            Path filePath = dirPath.resolve(fileName);
//
//            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
//
//            log.info("파일 저장 완료: {}", filePath);
//            return fileName; // DB에는 아직 저장 X
//        } catch (IOException e) {
//            throw new RuntimeException("파일 저장 실패", e);
//        }
//    }

    // ① 업로드 시 임시 파일명으로 저장
    public String saveTempFile(String type, MultipartFile file) {
        try {
            Path dir = Paths.get("/opt/ucamp/uploads", type);
            Files.createDirectories(dir);

            String ext = getExtOrDefault(file.getOriginalFilename(), ".png");
            String tempName = UUID.randomUUID() + ext; // 임시 파일명
            Path dest = dir.resolve(tempName);

            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
            return tempName; // tempFileName 반환
        } catch (IOException e) {
            throw new RuntimeException("파일 임시 저장 실패", e);
        }
    }

    // ② cert_id 확정 이후 임시 파일을 {cert_id}.{ext} 로 rename
    public String renameTempToCertId(String type, String tempFileName, Long certId) {
        try {
            Path dir = Paths.get("/opt/ucamp/uploads", type);
            Files.createDirectories(dir);

            String ext = getExtOrDefault(tempFileName, ".png");
            Path src = dir.resolve(tempFileName);
            String finalName = certId + ext; // 최종 파일명
            Path dst = dir.resolve(finalName);

            // 기존 동일 이름 파일 있으면 대체
            Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING);

            // 프론트에서 그대로 <img src>로 쓸 URL을 반환
            return "/image/" + type + "/" + finalName;
        } catch (IOException e) {
            throw new RuntimeException("임시 파일명 → cert_id 파일명 변경 실패", e);
        }
    }

    private String getExtOrDefault(String name, String def) {
        if (name == null) return def;
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot) : def;
    }
}
