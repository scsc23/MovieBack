package org.movieproject.post.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostDTO {

    private Integer postId;

    @NotEmpty
    private String postContent;

    @Min(value = 1)     //  최소값
    @Max(value = 5)     //  최대값
    private int ratingStar;

    private Integer movieId;

    private Integer memberNo;

    private String memberNick;

    // 등록 날짜 (문자열 형태로 전송)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate regDate;

    private String filePath;

    private String movieTitle;

}
