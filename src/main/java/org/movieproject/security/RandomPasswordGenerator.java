package org.movieproject.security;

import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class RandomPasswordGenerator {

    // 지정된 길이의 난수 문자열을 생성하는 메서드
    public String generateRandomPassword(int length) {
        SecureRandom secureRandom;
        try {
            secureRandom = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            // fallback to default SecureRandom instance
            secureRandom = new SecureRandom();
        }

        byte[] randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);

        // Base64 인코딩을 사용하여 난수 바이트 배열을 문자열로 변환
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
