package org.movieproject.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.movieproject.likes.entity.Likes;
import org.movieproject.post.entity.Post;
import org.movieproject.upload.entity.Image;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
@ToString(exclude = {"roleSet", "likes"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer memberNo;

    @Column(nullable = false)
    private String memberEmail;

    @Column(nullable = false)
    private String memberPw;

    @Column(nullable = false)
    private String memberName;

    @Column(nullable = false)
    private String memberPhone;

    @Column(nullable = false)
    private String memberNick;

    private boolean social;

    //  이미지 연관관계 설정
    //  회원 삭제 시 이미지 데이터도 같이 삭제되도록 구성
    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Image image;

    @ElementCollection(targetClass = Role.class, fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<Role> roleSet = new HashSet<>();

    public void addRole(Role role) {
        this.roleSet.add(role);
    }

    // 좋아요 (like) 참조
    // 다대일 정의/매핑, 역방향 관계 / member 엔티티의 변경이 like 엔티티에도 전파되도록 설정한 것
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Likes> likes = new ArrayList<>();

    // 좋아요 (like) 참조 END

    public List<Likes> getLikeMovies() {
        return likes;
    }

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Post> posts = new ArrayList<>();

}
