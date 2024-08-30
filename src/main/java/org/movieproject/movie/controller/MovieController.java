package org.movieproject.movie.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.movieproject.member.dto.MemberDTO;
import org.movieproject.movie.dto.MovieDTO;
import org.movieproject.movie.entity.Movie;
import org.movieproject.movie.repository.MovieRepository;
import org.movieproject.movie.service.MovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;
    private final ModelMapper modelMapper;

    @GetMapping("/now_playing")
    public CompletableFuture<List<Map<String, String>>> getNowPlayingMovies() {
        return movieService.getNowPlayingMovies()
                .thenApply(movies -> {
                    movieService.saveMovies(movies); // 검색된 영화를 저장
                    return movies;
                });
    }

    @GetMapping("/top_rated")
    public List<Integer> getTopRatedMovies() {
        return movieService.getTopRatedMovies();
    }

    @GetMapping("/top_liked")
    public List<Integer> getTopLikedMovies() {
        return movieService.getTopLikedMovies();
    }

    @GetMapping("/{movieId}")
    public CompletableFuture<Map<String, String>> getMovieByMovieId(@PathVariable Integer movieId) {
        return movieService.getMovieByMovieId(movieId);
    }

    @GetMapping("/search")
    public CompletableFuture<List<Map<String, String>>> searchMovies(@RequestParam String keyword, @RequestParam int page) {
        log.info("키워드: " + keyword + ", 페이지: " + page);
        return movieService.searchMovieByKeyword(keyword, page)
                .thenApply(movies -> {
                    movieService.saveMovies(movies); // 검색된 영화를 저장
                    return movies;
                });
    }

    @GetMapping("/videos/{movieId}")
    public CompletableFuture<List<String>> getVideosByMovieId(@PathVariable Integer movieId) {
        return movieService.getYoutubeVideoKeys(movieId);
    }

    @GetMapping("/images/{movieId}")
    public CompletableFuture<List<String>> getImagesByMovieId(@PathVariable Integer movieId) {
        return movieService.getMovieImages(movieId);
    }

    @GetMapping("/likes/{memberNo}")
    public ResponseEntity<List<Integer>> getLikes(@PathVariable Integer memberNo) {

        List<Integer> moviesIdList = movieService.getLikedMoviesByMemberNo(memberNo);
        log.info("무비들 !!!"+moviesIdList);

        return ResponseEntity.ok(moviesIdList);
    }
}