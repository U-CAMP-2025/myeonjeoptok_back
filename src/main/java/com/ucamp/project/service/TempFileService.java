package com.ucamp.project.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Slf4j
@Service
public class TempFileService {
    private final Path baseDir;

    public TempFileService() {
        this.baseDir = Path.of(System.getProperty("java.io.tmpdir"), "ucamp-audio");
        try { Files.createDirectories(baseDir); }
        catch (IOException e) { throw new RuntimeException("임시 디렉토리 생성 실패: " + baseDir, e); }
    }

    public Path saveToTemp(MultipartFile file, String prefix) {
        try {
            String ext = FilenameUtils.getExtension(file.getOriginalFilename());
            if (ext == null || ext.isBlank()) ext = "webm";
            Path temp = Files.createTempFile(baseDir, prefix + "_", "." + ext);
            file.transferTo(temp);
            log.info("Saved temp: {}", temp);
            return temp;
        } catch (IOException e) {
            throw new RuntimeException("임시 파일 저장 실패", e);
        }
    }

    public void safeDelete(Path path) {
        if (path == null) return;
        try { Files.deleteIfExists(path); }
        catch (Exception e) { log.warn("Temp delete failed: {}", path, e); }
    }
}
