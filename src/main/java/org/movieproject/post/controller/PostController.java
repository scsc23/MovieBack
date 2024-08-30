package org.movieproject.post.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.movieproject.post.dto.PageRequestDTO;
import org.movieproject.post.dto.PageResponseDTO;
import org.movieproject.post.dto.PostDTO;
import org.movieproject.post.service.PostService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
@Log4j2
public class PostController {

    private final PostService postService;


    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> register(@RequestBody PostDTO postsDTO) {
        log.info("컨트롤러 포스트디티오" + postsDTO.toString());
        postService.regPost(postsDTO);

        return ResponseEntity.ok(Map.of("message", "포스트 작성 완료"));
    }

    //  삭제
    @DeleteMapping(value = "/delete/{postId}")
    public ResponseEntity<Map<String, String>> remove(@PathVariable("postId") Integer postId) {
        postService.deletePost(postId);
        return ResponseEntity.ok(Map.of("message", "포스트 삭제 완료"));
    }

    //  검색 조건과 페이징 처리
//    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
//    public PageResponseDTO<PostDTO> list(PageRequestDTO pageRequestDTO) {return postsService.list(pageRequestDTO);}

    @GetMapping("/movie/{movieId}")
    public List<PostDTO> getPostByMovieId(@PathVariable("movieId") Integer movieId) {
        List<PostDTO> postDTOs = postService.getPostByMovieId(movieId);
        log.info("포스트리스트 from 컨트롤러: " + postDTOs.toString());
        if (postDTOs == null || postDTOs.isEmpty()) {
            return new ArrayList<>(); // 빈 리스트 반환
        }
        return postDTOs;
    }


    @GetMapping("/average-rating/{movieId}")
    public ResponseEntity<Double> getAverageRatingByMovieId(@PathVariable Integer movieId) {
        Double averageRating = postService.getAverageRatingByMovieId(movieId);
        return ResponseEntity.ok(averageRating);
    }

    @GetMapping("/{memberNo}")
    public ResponseEntity<?> getPostByMemberNo(@PathVariable("memberNo") Integer memberNo) {
        List<PostDTO> posts = postService.getPostsByMemberNo(memberNo);

        log.info("포스트들 !!! : "+ posts);

        return ResponseEntity.ok(posts);
    }
}