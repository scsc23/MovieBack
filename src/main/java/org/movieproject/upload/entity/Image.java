package org.movieproject.upload.entity;

import jakarta.persistence.*;
import lombok.*;
import org.movieproject.member.entity.Member;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "member")
public class Image{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer imageId;

    private String uuid;    //  이미지 중복을 방지하기 위한 고유값

    private String filePath;    //  파일 경로

    private String thumbnailPath;   //  썸네일 경로

    //  프로필 이미지는 회원 당 1개만 가질 수 있으므로 @OneToOne 적용
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberNo")
    private Member member;

    public void changeImage(String uuid, String filePath, String thumbnailPath){
        this.uuid = uuid;
        this.filePath = filePath;
        this.thumbnailPath = thumbnailPath;
    }

}