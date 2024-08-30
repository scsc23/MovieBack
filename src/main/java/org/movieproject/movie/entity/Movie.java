package org.movieproject.movie.entity;


import jakarta.persistence.*;
import lombok.*;
import org.movieproject.likes.entity.Likes;
import org.movieproject.post.entity.Post;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"post", "likes"})
public class Movie {

    @Id
    private Integer movieId;

    @Column(nullable = false)
    private String movieTitle;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Post> post = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Likes> likes = new ArrayList<>();

    public long getLikesCount() {
        return likes.stream().filter(Likes::isLiked).count();
    }
}
