package com.ucamp.project.controller;

import com.nimbusds.jose.util.Pair;
import com.ucamp.project.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileService fileService;

    @GetMapping("/image/{type}/{id}")
    public ResponseEntity<?> image(@PathVariable String type, @PathVariable String id) {

        Pair<String,Resource> pair = fileService.getImg(type, id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(pair.getLeft()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + pair.getRight().getFilename() + "\"")
                .body(pair.getRight());

    }
}
