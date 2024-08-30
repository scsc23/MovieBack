package org.movieproject.post.repository;

import org.movieproject.movie.entity.Movie;
import org.movieproject.post.entity.Post;
import org.movieproject.post.repository.search.PostSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Integer>, PostSearch {

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.member m LEFT JOIN FETCH m.image i WHERE p.movie.movieId = :movieId ORDER BY p.postId DESC")
    List<Post> findPostsByMovieId(Integer movieId);

    @Query("SELECT AVG(p.ratingStar) FROM Post p WHERE p.movie.movieId = :movieId")
    Double findAverageRatingByMovieId(Integer movieId);

    @Query("select p from Post p join fetch p.movie where p.member.memberNo = :memberNo")
    List<Post> findPostsByMemberNo(@Param("memberNo") Integer memberNo);

    //    @Query("select p from Post p where p.postTitle like concat('%', :keyword, '%')")
    //    Page<Post> findKeyword(String keyword, Pageable pageable);
}
