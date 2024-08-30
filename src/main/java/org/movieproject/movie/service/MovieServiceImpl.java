package org.movieproject.movie.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.movieproject.movie.entity.Movie;
import org.movieproject.movie.repository.MovieRepository;
import org.movieproject.post.repository.PostRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {
    private final WebClient.Builder webClientBuilder;
    private final MovieRepository movieRepository;

    @Value("${apiKey}")
    private String API_KEY;

    private WebClient webClient;

    @PostConstruct
    private WebClient getWebClient() {
        if (this.webClient == null) {
            this.webClient = webClientBuilder.baseUrl("https://api.themoviedb.org/3").build();
        }
        return this.webClient;
    }

    @Async
    @Override
    public CompletableFuture<List<Map<String, String>>> getNowPlayingMovies() {
        return this.getWebClient().get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/now_playing")
                        .queryParam("language", "ko-KR")
                        .queryParam("page", 1)
                        .queryParam("api_key", API_KEY)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(response -> {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                    return results.stream()
                            .map(movie -> Map.of(
                                    "id", String.valueOf(movie.get("id")),
                                    "title", String.valueOf(movie.get("title")),
                                    "poster_path", String.valueOf(movie.get("poster_path"))
                            ))
                            .collect(Collectors.toList());
                })
                .toFuture();
    }

    @Override
    public List<Integer> getTopRatedMovies() {
        return movieRepository.findMoviesOrderByRatingStarAvgDesc();
    }

    @Override
    public List<Integer> getTopLikedMovies() {
        return movieRepository.findMoviesOrderByLikesDesc();
    }

    public void saveMovies(List<Map<String, String>> movies) {
        List<Movie> movieEntities = movies.stream()
                .map(movie -> {
                    Integer movieId = Integer.parseInt(movie.get("id"));
                    // 중복 체크: DB에 해당 movieId가 있는지 확인
                    if (movieRepository.existsById(movieId)) {
                        return null; // 이미 존재하면 null 반환
                    }
                    Movie movieEntity = new Movie();
                    movieEntity.setMovieId(movieId);
                    movieEntity.setMovieTitle(movie.get("title"));
                    return movieEntity;
                })
                .filter(Objects::nonNull) // null이 아닌 영화만 필터링
                .collect(Collectors.toList());

        if (!movieEntities.isEmpty()) {
            movieRepository.saveAll(movieEntities);
        }
    }

    @Async
    @Override
    public CompletableFuture<Map<String, String>> getMovieByMovieId(Integer movieId) {
        return this.getWebClient().get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/{id}")
                        .queryParam("language", "ko-KR")
                        .queryParam("api_key", API_KEY)
                        .build(movieId))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(movie -> Map.of(
                        "id", String.valueOf(movie.get("id")),
                        "title", String.valueOf(movie.get("title")),
                        "overview", String.valueOf(movie.get("overview")),
                        "poster_path", String.valueOf(movie.get("poster_path"))
                ))
                .toFuture();
    }

    @Async
    @Override
    public CompletableFuture<List<Map<String, String>>> searchMovieByKeyword(String keyword, int page) {
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        log.info("인코드키워드 " + encodedKeyword);
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/movie")
                        .queryParam("query", encodedKeyword)
                        .queryParam("language", "ko-KR")
                        .queryParam("page", page)
                        .queryParam("api_key", API_KEY)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(response -> {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                    return results.stream()
                            .map(movie -> {
                                Map<String, String> movieInfo = new HashMap<>();
                                movieInfo.put("id", String.valueOf(movie.get("id")));
                                movieInfo.put("title", String.valueOf(movie.get("title")));
                                movieInfo.put("poster_path", String.valueOf(movie.get("poster_path")));
                                movieInfo.put("overview", String.valueOf(movie.get("overview")));
                                return movieInfo;
                            })
                            .collect(Collectors.toList());
                })
                .toFuture();
    }

    @Async
    @Override
    public CompletableFuture<List<String>> getYoutubeVideoKeys(Integer movieId) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/{id}/videos")
                        .queryParam("language", "ko-KR")
                        .queryParam("api_key", API_KEY)
                        .build(movieId))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(response -> {
                    @SuppressWarnings("unchecked")
                    List<Map<String, String>> results = (List<Map<String, String>>) response.get("results");

                    return results.stream()
                            .filter(result -> "YouTube".equalsIgnoreCase(result.get("site")))
                            .map(result -> result.get("key"))
                            .collect(Collectors.toList());
                })
                .toFuture();
    }

    @Async
    @Override
    public CompletableFuture<List<String>> getMovieImages(Integer movieId) {
        return webClientBuilder.build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/{id}/images")
                        .queryParam("api_key", API_KEY)
                        .build(movieId))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(response -> {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> backdrops = (List<Map<String, Object>>) response.get("backdrops");

                    return backdrops.stream()
                            .filter(image -> image.get("iso_639_1") == null && ((Integer) image.get("height")) < ((Integer) image.get("width")))
                            .map(image -> (String) image.get("file_path"))
                            .collect(Collectors.toList());
                })
                .toFuture();
    }

    @Override
    public List<Integer> getLikedMoviesByMemberNo(Integer memberNo){
        List<Movie> movies = movieRepository.findLikedMoviesByMemberNo(memberNo);  // 회원이 좋아요를 누른 영화 목록을 가져옴
        List<Integer> movieIds = new ArrayList<>();  // 영화 ID를 저장할 리스트

        for(Movie movieEntity : movies){
            Integer movieId = movieEntity.getMovieId();  // 각 영화 객체의 movieId를 가져옴
            movieIds.add(movieId);  // movieIds 리스트에 추가
        }
        return movieIds;
    }
}
