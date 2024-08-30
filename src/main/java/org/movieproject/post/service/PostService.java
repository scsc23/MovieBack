package org.movieproject.post.service;

import org.movieproject.post.dto.PageRequestDTO;
import org.movieproject.post.dto.PageResponseDTO;
import org.movieproject.post.dto.PostDTO;

import java.util.List;

public interface PostService {

    //  등록
    void regPost(PostDTO postsDTO);

    //  삭제
    void deletePost(Integer postId);

    //  Paging
//    PageResponseDTO<PostDTO> list (PageRequestDTO pageRequestDTO);

    // 영화 ID로 포스트 찾기
    List<PostDTO> getPostByMovieId(Integer movieId);

    // 영화에 대한 포스트 별점 평균
    Double getAverageRatingByMovieId(Integer movieId);

    // 회원 별 포스트 찾기
    List<PostDTO> getPostsByMemberNo(Integer memberNo);

}
