package com.ucamp.project.service;

import com.nimbusds.jose.util.Pair;
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
public class FileService {

    public Pair<String,Resource> getImg(String type, String id) {


        String url;
        Resource imageResource;
        String contentType;

        if("cert".equals(type)){
            url = "src/main/resources/static/image/"+type+"/" + id;
        } else {
            url = "src/main/resources/static/image/"+type+"/" + id + ".png";
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


    public String saveFile(String type, MultipartFile file) {
        try {
            Path dirPath = Paths.get("src/main/resources/static/image/" + type);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // UUID로 파일명 중복 방지
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = dirPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }
}
