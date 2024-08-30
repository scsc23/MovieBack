package org.movieproject.upload.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.movieproject.upload.dto.UploadResultDTO;
import org.movieproject.upload.entity.Image;
import org.movieproject.upload.service.ImageService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
@Log4j2
public class UpDownController {

    private final ImageService imageService;

    // 이미지 업로드 및 수정
    @PostMapping("/upload")
    public ResponseEntity<UploadResultDTO> uploadOrUpdateImage(@RequestParam("file") MultipartFile file,
                                                               @RequestParam("memberNo") Integer memberNo) {
        UploadResultDTO result = imageService.uploadImage(file, memberNo);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // 이미지 조회
    @GetMapping("/read/{memberNo}")
    public ResponseEntity<Resource> readImage(@PathVariable Integer memberNo) throws IOException {
        Image image = imageService.getImage(memberNo);
        if (image != null) {
            Path filePath = Paths.get(image.getFilePath());
            Resource resource = new FileSystemResource(filePath);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, Files.probeContentType(filePath));
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filePath.getFileName().toString() + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(resource.contentLength())
                    .contentType(MediaType.parseMediaType(Files.probeContentType(filePath)))
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 이미지 삭제
    @DeleteMapping("/delete/{memberNo}")
    public ResponseEntity<Void> deleteImage(@PathVariable Integer memberNo) {
        imageService.deleteImage(memberNo);
        return ResponseEntity.noContent().build();
    }

    // 예외 처리
    @ExceptionHandler({ IllegalArgumentException.class, RuntimeException.class })
    public ResponseEntity<String> handleException(Exception ex) {
        log.error("Exception occurred: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

}