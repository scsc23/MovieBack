package org.movieproject.member.service;


import org.movieproject.member.dto.MemberDTO;
import org.movieproject.upload.dto.UploadResultDTO;

public interface MemberService {

    // 아이디/닉네임 중복확인
    public class MemberExistException extends Exception {
        public MemberExistException() {
            super();
        }
        public MemberExistException(String message) {
            super(message);
        }
    }

    // 회원가입
    void memberJoin(MemberDTO memberDTO) throws MemberService.MemberExistException;

    // 회원정보 삭제
    void deleteMember(Integer memberNo) throws MemberService.MemberExistException;

}
