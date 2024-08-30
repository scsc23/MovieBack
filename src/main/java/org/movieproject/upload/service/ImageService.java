package org.movieproject.upload.service;

import org.movieproject.member.entity.Member;
import org.movieproject.upload.dto.UploadResultDTO;
import org.movieproject.upload.entity.Image;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ImageService {

    //  이미지 업로드
    UploadResultDTO uploadImage(MultipartFile file, Integer memberNo);

    //  이미지 조회
    Image getImage(Integer memberNo);

    //  이미지 삭제
    void deleteImage(Integer memberNo);


}