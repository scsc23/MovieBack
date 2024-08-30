package org.movieproject.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.movieproject.member.entity.Member;
import org.movieproject.member.dto.MemberSecurityDTO;
import org.movieproject.member.repository.MemberRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class MvpUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("loadUserByUsername : " + username);

        Optional<Member> memberResult = memberRepository.findByMemberEmailWithRoles(username);
        if (memberResult.isPresent()) {
            Member member = memberResult.get();
            MemberSecurityDTO memberSecurityDTO = new MemberSecurityDTO(
                    member.getMemberNo(),
                    member.getMemberEmail(),
                    member.getMemberPw(),
                    member.getMemberName(),
                    member.getMemberPhone(),
                    member.getMemberNick(),
                    false,
                    member.getRoleSet().stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                            .collect(Collectors.toList())
            );

            log.info("멤버 시큐리티 DTO: " + memberSecurityDTO);
            return memberSecurityDTO;
        }

        throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
    }

}