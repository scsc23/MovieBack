package org.movieproject.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.movieproject.member.dto.MemberSecurityDTO;
import org.movieproject.member.entity.Member;
import org.movieproject.member.entity.Role;
import org.movieproject.member.repository.MemberRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class MvpOIDCUserService extends OidcUserService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RandomPasswordGenerator randomPasswordGenerator;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        log.info("로드된 OIDC 유저: {}", oidcUser);

        String memberEmail = oidcUser.getEmail();
        Map<String, Object> attributes = oidcUser.getAttributes();

        return memberSecurityDTO(memberEmail, attributes);
    }

    private MemberSecurityDTO memberSecurityDTO(String memberEmail, Map<String, Object> attributes) {
        Optional<Member> result = memberRepository.findByMemberEmail(memberEmail);
        log.info("결과: {}", result);

        if (result.isEmpty()) {
            String randomPassword = passwordEncoder.encode(randomPasswordGenerator.generateRandomPassword(8));
            Member member = Member.builder()
                    .memberNo(null)
                    .memberEmail(memberEmail)
                    .memberPw(randomPassword)
                    .memberPhone("01012345678")
                    .memberName("임시 사용자")
                    .memberNick("임시 사용자")
                    .social(true)
                    .build();

            member.addRole(Role.GUEST);
            log.info("멤버: {}", member);
            memberRepository.save(member);

            MemberSecurityDTO memberSecurityDTO = new MemberSecurityDTO(
                    null, memberEmail, randomPassword, "임시 사용자",
                    "01012345678", "임시 사용자", true,
                    List.of(new SimpleGrantedAuthority("ROLE_GUEST")));
            log.info("멤버 시큐리티 DTO: {}", memberSecurityDTO);
            return memberSecurityDTO;
        } else {
            Member member = result.get();

            MemberSecurityDTO memberSecurityDTO = new MemberSecurityDTO(
                    member.getMemberNo(), member.getMemberEmail(), member.getMemberPw(),
                    member.getMemberPhone(), member.getMemberName(), member.getMemberNick(), member.isSocial(),
                    member.getRoleSet().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                            .collect(Collectors.toList()));

            memberSecurityDTO.setProps(attributes);
            log.info("멤버 시큐리티 DTO: {}", memberSecurityDTO);
            return memberSecurityDTO;
        }
    }
}
