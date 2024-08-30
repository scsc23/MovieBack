package org.movieproject.member.dto;

import lombok.*;
import org.movieproject.member.entity.Role;

import java.util.Set;

@Data
public class MemberDTO {
    private Integer memberNo;

    private String memberEmail;

    private String memberPw;

    private String memberName;

    private String memberPhone;

    private String memberNick;

    private Set<Role> roleSet;
}
