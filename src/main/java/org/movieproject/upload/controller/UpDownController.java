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

import java.io.File;
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
                                                               @RequestParam("memberNo") Integer memberNo) throws IOException {
        // 이미지 업로드 및 결과 받기
        UploadResultDTO result = imageService.uploadImage(file, memberNo);

        // 임시 파일 경로 얻기
        String tempFilePath = Paths.get(System.getProperty("java.io.tmpdir"), "images", file.getOriginalFilename()).toString();

        // 임시 파일 삭제
        File tempFile = new File(tempFilePath);
        if (tempFile.exists()) {
            Files.delete(tempFile.toPath());
            log.info("Temporary file deleted: {}", tempFilePath);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/read/{memberNo}")
    public ResponseEntity<Resource> readImage(@PathVariable Integer memberNo) throws IOException {
        Image image = imageService.getImage(memberNo);
        if (image != null) {
            InputStream inputStream = imageService.getImageInputStream(image.getFilePath());

            Resource resource = new InputStreamResource(inputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, Files.probeContentType(Paths.get(image.getFilePath())));
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + image.getUuid() + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(Files.probeContentType(Paths.get(image.getFilePath()))))
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

    @ExceptionHandler({ IllegalArgumentException.class, RuntimeException.class })
    public ResponseEntity<String> handleException(Exception ex) {
        log.error("Exception occurred: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

}
