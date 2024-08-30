package org.movieproject.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.movieproject.member.entity.Member;
import org.movieproject.member.entity.Role;
import org.movieproject.member.dto.MemberSecurityDTO;
import org.movieproject.member.repository.MemberRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
@Transactional
public class MvpOauth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RandomPasswordGenerator randomPasswordGenerator;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        log.info("Oauth2 유 저 요 청");
        log.info(userRequest);

        ClientRegistration clientRegistration = userRequest.getClientRegistration();
        String clientName = clientRegistration.getClientName();

        log.info("클라이언트네임 : " + clientName);

        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> paramMap = oAuth2User.getAttributes();

        String memberEmail = switch (clientName) {
            case "kakao" -> getKakaoEmail(paramMap);
            case "naver" -> getNaverEmail(paramMap);
            default -> null;
        };

        log.info("이 메 일 : " + memberEmail);

        return memberSecurityDTO(memberEmail, paramMap);
    }

    // 소셜 회원 가입
    private MemberSecurityDTO memberSecurityDTO (String memberEmail, Map<String, Object> params) {

        Optional<Member> result = memberRepository.findByMemberEmail(memberEmail);
        log.info("리 절 트" + result);

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
            log.info("멤버멤버" + member);
            memberRepository.save(member);

            MemberSecurityDTO memberSecurityDTO = new MemberSecurityDTO(
                    null, memberEmail, randomPassword, "임시 사용자",
                    "01012345678",  "임시 사용자", true,
                    List.of(new SimpleGrantedAuthority("ROLE_GUEST")));
            log.info("멤버시큐리티디티오" + memberSecurityDTO);
            return memberSecurityDTO;
        } else {
            Member member = result.get();

            MemberSecurityDTO memberSecurityDTO = new MemberSecurityDTO(
                    member.getMemberNo(), member.getMemberEmail(), member.getMemberPw(),
                    member.getMemberPhone(), member.getMemberName(), member.getMemberNick(), member.isSocial(),
                    member.getRoleSet().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                            .collect(Collectors.toList()));

            memberSecurityDTO.setProps(params);
            log.info("멤버시큐리티디티오222" + memberSecurityDTO);
            return memberSecurityDTO;
        }
    }

    private String getKakaoEmail(Map<String, Object> paramMap) {

        log.info("카카오 이메일 가져오기 시작");

        Object value = paramMap.get("kakao_account");

        LinkedHashMap accountMap = (LinkedHashMap)value;

        String email = (String)accountMap.get("email");

        log.info("카카오 이메일 : " + email);

        return email;
    }

    private String getNaverEmail(Map<String, Object> paramMap) {
        log.info("네이버 이메일 가져오기 시작");

        Object value = paramMap.get("response");

        LinkedHashMap accountMap = (LinkedHashMap)value;

        String email = (String)accountMap.get("email");

        log.info("네이버 이메일: " + email);

        return email;
    }
}
