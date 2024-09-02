package org.movieproject.upload.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.movieproject.upload.dto.UploadResultDTO;
import org.movieproject.upload.entity.Image;
import org.movieproject.upload.service.ImageService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
@Log4j2
public class UpDownController {

    private final ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<UploadResultDTO> uploadOrUpdateImage(@RequestParam("file") MultipartFile file,
                                                               @RequestParam("memberNo") Integer memberNo) throws IOException {
        // 이미지 업로드 및 결과 받기
        UploadResultDTO result = imageService.uploadImage(file, memberNo);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/read/{memberNo}")
    public ResponseEntity<Resource> readImage(@PathVariable Integer memberNo) throws IOException {
        Image image = imageService.getImage(memberNo);
        if (image != null) {
            // 파일명 디코딩
            String decodedFilePath = URLDecoder.decode(image.getFilePath(), StandardCharsets.UTF_8);

            // S3에서 파일 스트림 가져오기
            InputStream inputStream = imageService.getImageInputStream(decodedFilePath);

            Resource resource = new InputStreamResource(inputStream);

            // 콘텐츠 타입 설정
            String contentType = Files.probeContentType(Paths.get(decodedFilePath));
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, contentType);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + image.getUuid() + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete/{memberNo}")
    public ResponseEntity<Void> deleteImage(@PathVariable Integer memberNo) {
        Image image = imageService.getImage(memberNo);
        if (image != null) {
            imageService.deleteImage(memberNo);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @ExceptionHandler({ IllegalArgumentException.class, RuntimeException.class })
    public ResponseEntity<String> handleException(Exception ex) {
        log.error("Exception occurred: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
