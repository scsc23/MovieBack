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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
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
                                                               @RequestParam("memberNo") Integer memberNo) {
        UploadResultDTO result = imageService.uploadImage(file, memberNo);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/read/{memberNo}")
    public ResponseEntity<Resource> readImage(@PathVariable Integer memberNo) throws IOException {
        Image image = imageService.getImage(memberNo);
        if (image != null) {
            // S3에서 이미지를 가져오기 위한 InputStream 얻기
            InputStream inputStream = imageService.getImageInputStream(image.getFilePath());

            Resource resource = new InputStreamResource(inputStream);

            // MIME 타입을 동적으로 가져오기 위해 파일 경로에서 추론
            String contentType = Files.probeContentType(Paths.get(image.getFilePath()));
            if (contentType == null) {
                contentType = "application/octet-stream"; // 기본값 설정
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, contentType);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + image.getUuid() + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete/{memberNo}")
    public ResponseEntity<Void> deleteImage(@PathVariable Integer memberNo) {
        imageService.deleteImage(memberNo);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler({IllegalArgumentException.class, RuntimeException.class})
    public ResponseEntity<String> handleException(Exception ex) {
        log.error("Exception occurred: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
