package org.movieproject.upload.service;

import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.movieproject.member.entity.Member;
import org.movieproject.member.repository.MemberRepository;
import org.movieproject.upload.dto.UploadResultDTO;
import org.movieproject.upload.entity.Image;
import org.movieproject.upload.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final MemberRepository memberRepository;

    @Value("${org.movieproject.file.path}")
    private String uploadDirectory;

    // 이미지 업로드 기능 구현
    @Override
    public UploadResultDTO uploadImage(MultipartFile file, Integer memberNo) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        Member member = memberRepository.findById(memberNo)
                .orElseThrow(() -> new IllegalArgumentException("Member Not Found"));

        Image existingImage = imageRepository.findByMember_memberNo(memberNo);

        try {
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();

            // 확장자 검사
            if (!isSupportedFileType(fileExtension)) {
                throw new IllegalArgumentException("Only jpg, jpeg, png, gif files are allowed");
            }

            String uuid = UUID.randomUUID().toString();
            String fileName = uuid + "_" + originalFileName;

            Path uploadPath = Paths.get(uploadDirectory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            // 썸네일 생성
            Path thumbnailFilePath = uploadPath.resolve("thumb_" + fileName);
            Thumbnails.of(filePath.toFile())
                    .size(100, 100) // 썸네일 크기 설정
                    .toFile(thumbnailFilePath.toFile());

            Image image;
            if (existingImage == null) {
                // 새로운 이미지 업로드
                image = Image.builder()
                        .uuid(uuid)
                        .filePath(filePath.toString())
                        .thumbnailPath(thumbnailFilePath.toString())
                        .member(member)
                        .build();
            } else {
                // 이미지 업데이트
                // 기존 파일 삭제
                Path oldFilePath = Paths.get(existingImage.getFilePath());
                Files.deleteIfExists(oldFilePath);

                // 기존 썸네일 파일 삭제
                Path oldThumbnailFilePath = Paths.get(existingImage.getThumbnailPath());
                Files.deleteIfExists(oldThumbnailFilePath);

                // 새로운 썸네일 생성
                Thumbnails.of(filePath.toFile())
                        .size(100, 100) // 썸네일 크기 설정
                        .toFile(thumbnailFilePath.toFile());

                // 이미지 정보 업데이트
                existingImage.changeImage(uuid, filePath.toString(), thumbnailFilePath.toString());
                image = existingImage;
            }

            imageRepository.save(image);

            return UploadResultDTO.builder()
                    .uuid(uuid)
                    .filePath(filePath.toString())
                    .thumbnailPath(thumbnailFilePath.toString())
                    .memberNo(memberNo)
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    // 이미지 삭제
    @Override
    public void deleteImage(Integer memberNo) {
        Image image = imageRepository.findByMember_memberNo(memberNo);
        if (image == null) {
            throw new IllegalArgumentException("Image Not Found");
        }

        Path filePath = Paths.get(image.getFilePath());
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file", e);
        }
        imageRepository.delete(image);
    }

    // 이미지 조회
    @Override
    public Image getImage(Integer memberNo) {
        return imageRepository.findByMember_memberNo(memberNo);
    }

    // 허용된 파일 확장자 목록 검사
    private boolean isSupportedFileType(String fileExtension) {
        List<String> supportedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif");
        return supportedExtensions.contains(fileExtension);
    }
}