package org.movieproject.upload.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
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
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final MemberRepository memberRepository;
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Override
    public UploadResultDTO uploadImage(MultipartFile file, Integer memberNo) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        Member member = memberRepository.findById(memberNo)
                .orElseThrow(() -> new IllegalArgumentException("Member Not Found"));

        Image existingImage = imageRepository.findByMember_memberNo(memberNo);

        try {
            // 파일명 URL 인코딩
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();

            if (!isSupportedFileType(fileExtension)) {
                throw new IllegalArgumentException("Only jpg, jpeg, png, gif files are allowed");
            }

            String uuid = UUID.randomUUID().toString();
            String encodedFileName = URLEncoder.encode(uuid + "_" + originalFileName, StandardCharsets.UTF_8);

            // S3에 파일 업로드
            String fileUrl = uploadFileToS3(file, encodedFileName);

            // 썸네일 생성 및 업로드
            String thumbnailFileName = URLEncoder.encode("thumb_" + uuid + "_" + originalFileName, StandardCharsets.UTF_8);
            String thumbnailFileUrl = createAndUploadThumbnail(file, thumbnailFileName);

            Image image;
            if (existingImage == null) {
                image = Image.builder()
                        .uuid(uuid)
                        .filePath(fileUrl)
                        .thumbnailPath(thumbnailFileUrl)
                        .member(member)
                        .build();
            } else {
                // 기존 파일과 썸네일 삭제
                deleteFileFromS3(existingImage.getFilePath());
                deleteFileFromS3(existingImage.getThumbnailPath());

                existingImage.changeImage(uuid, fileUrl, thumbnailFileUrl);
                image = existingImage;
            }

            imageRepository.save(image);

            return UploadResultDTO.builder()
                    .uuid(uuid)
                    .filePath(fileUrl)
                    .thumbnailPath(thumbnailFileUrl)
                    .memberNo(memberNo)
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
    public void deleteImage(Integer memberNo) {
        Image image = imageRepository.findByMember_memberNo(memberNo);
        if (image == null) {
            throw new IllegalArgumentException("Image Not Found");
        }

        deleteFileFromS3(image.getFilePath());
        deleteFileFromS3(image.getThumbnailPath());

        imageRepository.delete(image);
    }

    @Override
    public Image getImage(Integer memberNo) {
        return imageRepository.findByMember_memberNo(memberNo);
    }

    @Override
    public InputStream getImageInputStream(String filePath) {
        return amazonS3.getObject(new GetObjectRequest(bucketName, extractFileNameFromUrl(filePath))).getObjectContent();
    }

    private String uploadFileToS3(MultipartFile file, String fileName) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            amazonS3.putObject(bucketName, fileName, inputStream, metadata);
            return amazonS3.getUrl(bucketName, fileName).toString();
        }
    }

    private String createAndUploadThumbnail(MultipartFile file, String thumbnailFileName) throws IOException {
        // S3에 직접 업로드하는 대신, 임시로 생성한 썸네일을 S3에 업로드
        Path tempThumbnailPath = Files.createTempFile(thumbnailFileName, ".tmp");
        Thumbnails.of(file.getInputStream())
                .size(100, 100)
                .toFile(tempThumbnailPath.toFile());

        try (InputStream thumbnailInputStream = Files.newInputStream(tempThumbnailPath)) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(Files.size(tempThumbnailPath));
            metadata.setContentType(Files.probeContentType(tempThumbnailPath));

            amazonS3.putObject(bucketName, thumbnailFileName, thumbnailInputStream, metadata);
            return amazonS3.getUrl(bucketName, thumbnailFileName).toString();
        } finally {
            Files.deleteIfExists(tempThumbnailPath);
        }
    }

    private void deleteFileFromS3(String fileUrl) {
        String fileName = extractFileNameFromUrl(fileUrl);
        amazonS3.deleteObject(bucketName, fileName);
    }

    private String extractFileNameFromUrl(String fileUrl) {
        return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
    }

    private boolean isSupportedFileType(String fileExtension) {
        List<String> supportedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif");
        return supportedExtensions.contains(fileExtension);
    }
}
