package org.movieproject.likes.repository;

import org.movieproject.likes.entity.Likes;
import org.movieproject.member.entity.Member;
import org.movieproject.movie.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LikesRepository extends JpaRepository<Likes, Integer> {

    @Query("SELECT l FROM Likes l WHERE l.member = :member AND l.movie = :movie")
    Optional<Likes> findByMemberAndMovie(@Param("member") Member member, @Param("movie") Movie movie);

    @Query("SELECT COUNT(l) FROM Likes l WHERE l.movie.movieId = :movieId AND l.liked = true")
    Integer countByMovieIdAndLiked(@Param("movieId") Integer movieId);
}
