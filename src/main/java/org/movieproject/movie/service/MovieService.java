package org.movieproject.movie.service;

import org.movieproject.movie.entity.Movie;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface MovieService {
    CompletableFuture<List<Map<String, String>>> getNowPlayingMovies();

    List<Integer> getTopRatedMovies();

    List<Integer> getTopLikedMovies();

    CompletableFuture<Map<String, String>> getMovieByMovieId(Integer movieId);

    CompletableFuture<List<Map<String, String>>> searchMovieByKeyword(String keyword, int page);

    void saveMovies(List<Map<String, String>> movies);

    CompletableFuture<List<String>> getYoutubeVideoKeys(Integer movieId);

    CompletableFuture<List<String>> getMovieImages(Integer movieId);

    List<Integer> getLikedMoviesByMemberNo(Integer memberNo);
}
