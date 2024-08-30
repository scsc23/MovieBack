package org.movieproject.upload.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class UploadDTO {

    //  Swagger-UI와 같은 프레임워크를 이용한 테스트를 위해 별도의 DTO 생성
    List<MultipartFile> files;
}
