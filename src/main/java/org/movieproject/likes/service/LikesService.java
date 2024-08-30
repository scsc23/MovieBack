package org.movieproject.likes.service;

import org.movieproject.likes.dto.LikesDTO;
import org.movieproject.likes.entity.Likes;
import org.movieproject.member.entity.Member;

import java.util.List;

public interface LikesService {

    void toggleLike(LikesDTO likesDTO);

    boolean getLikeStatus(Integer memberNo, Integer movieId);

    Integer getMovieLikesCount(Integer movieId);

//    Likes addLikes(LikesDTO likeDTO);
//
//    void removeLikesByMember(int memberNo); // 수정된 메서드 추가

}
