package org.movieproject.likes.entity;

import jakarta.persistence.*;
import lombok.*;
import org.movieproject.member.entity.Member;
import org.movieproject.member.entity.QMember;
import org.movieproject.movie.entity.Movie;

@Data
@Entity
@Getter
@ToString(exclude = {"member","movie"})
@AllArgsConstructor
@NoArgsConstructor
public class Likes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer likeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_no", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    private boolean liked;
}
