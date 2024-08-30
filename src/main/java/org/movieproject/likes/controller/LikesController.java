package org.movieproject.likes.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.movieproject.likes.dto.LikesDTO;
import org.movieproject.likes.entity.Likes;
import org.movieproject.likes.repository.LikesRepository;
import org.movieproject.likes.service.LikesService;
import org.movieproject.member.entity.Member;
import org.movieproject.member.repository.MemberRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/likes")
public class LikesController {

    private final LikesService likesService;
    private final LikesRepository likesRepository;
    private final MemberRepository memberRepository;

    @PostMapping("/update")
    public ResponseEntity<String> likeMovie(@RequestBody LikesDTO likesDTO) {
        likesService.toggleLike(likesDTO);
        return ResponseEntity.ok("Like status updated successfully");
    }

    @GetMapping("/status")
    public ResponseEntity<Boolean> getLikeStatus(@RequestParam Integer memberNo, @RequestParam Integer movieId) {
        boolean liked = likesService.getLikeStatus(memberNo, movieId);
        return ResponseEntity.ok(liked);
    }

    @GetMapping("/likesMovie")
    public ResponseEntity<Integer> getMovieLikes(@RequestParam Integer movieId) {
        Integer likeCount = likesService.getMovieLikesCount(movieId);
        return ResponseEntity.ok(likeCount);
    }
}