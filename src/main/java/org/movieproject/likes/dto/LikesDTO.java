package org.movieproject.likes.dto;

import lombok.Data;

@Data
public class LikesDTO {
    private Integer likesId;

    private Integer memberNo;

    private Integer movieId;

    private boolean liked;

}