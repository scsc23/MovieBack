package org.movieproject.member.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
@Setter
@ToString
public class MemberSecurityDTO extends User implements OAuth2User, OidcUser { // Security 용도로만 사용되는 DTO
    private Integer memberNo;

    private String memberEmail;

    private String memberPw;

    private String memberPhone;

    private String memberName;

    private String memberNick;

    private boolean social;

    private Map<String, Object> props;

    public MemberSecurityDTO(Integer memberNo, String username, String password,
                          String memberName, String memberPhone, String memberNick, boolean social,
                          Collection<? extends GrantedAuthority> authorities) {

        super(username, password, authorities);

        this.memberNo = memberNo;
        this.memberEmail = username;
        this.memberPw = password;
        this.memberName = memberName;
        this.memberPhone = memberPhone;
        this.memberNick = memberNick;
        this.social = social;
    }


    @Override
    public Map<String, Object> getAttributes() {
        return this.getProps();
    }

    @Override
    public String getName() {
        return this.memberEmail;
    }

    @Override
    public Map<String, Object> getClaims() {
        return Map.of();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return null;
    }

    @Override
    public OidcIdToken getIdToken() {
        return null;
    }
}