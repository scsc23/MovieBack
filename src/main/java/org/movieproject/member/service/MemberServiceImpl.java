package org.movieproject.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.movieproject.member.entity.Member;
import org.movieproject.member.dto.MemberDTO;
import org.movieproject.member.repository.MemberRepository;
import org.movieproject.upload.repository.ImageRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final ImageRepository imageRepository;


    // 회원가입
    @Override
    public void memberJoin(MemberDTO memberDTO) throws MemberExistException {
        // 아이디 중복확인
        String email = memberDTO.getMemberEmail();
        boolean exist = memberRepository.existsByMemberEmail(email);

        if (exist) {
            throw new MemberService.MemberExistException("아이디가 중복되었습니다 !!! ");
        }

        // 비밀번호 암호화
        String encodePassword = passwordEncoder.encode(memberDTO.getMemberPw());
        memberDTO.setMemberPw(encodePassword);
        log.info("매핑 전 !!!: {}", memberDTO);

        // 명시적 매핑
        Member member = modelMapper.map(memberDTO, Member.class);
        log.info("매핑 후 !!!: {}", member);

        memberRepository.save(member);

    }

    @Override
    public void deleteMember(Integer memberNo) {
        memberRepository.deleteById(memberNo);
    }
}