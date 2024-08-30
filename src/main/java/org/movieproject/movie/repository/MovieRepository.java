package org.movieproject.movie.repository;

import org.movieproject.likes.entity.Likes;
import org.movieproject.movie.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Integer> {

    @Query("SELECT m FROM Movie m WHERE m.movieId = :movieId")
    Optional<Movie> findMovieByMovieId(Integer movieId);

    @Query("SELECT m FROM Movie m JOIN m.likes l WHERE l.member.memberNo = :memberNo AND l.liked = true")
    List<Movie> findLikedMoviesByMemberNo(@Param("memberNo") Integer memberNo);

    @Query("SELECT m.movieId FROM Movie m INNER JOIN m.likes l ON l.liked = true GROUP BY m.movieId ORDER BY COUNT(l) DESC")
    List<Integer> findMoviesOrderByLikesDesc();

    @Query("SELECT m.movieId FROM Movie m JOIN m.post p GROUP BY m.movieId ORDER BY AVG(p.ratingStar) DESC")
    List<Integer> findMoviesOrderByRatingStarAvgDesc();
}
