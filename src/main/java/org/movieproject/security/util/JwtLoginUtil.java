package org.movieproject.security.util;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.movieproject.security.JwtProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Log4j2
@Component
@RequiredArgsConstructor
public class JwtLoginUtil {

    private final JwtProvider jwtProvider;
    private final Gson gson = new Gson();

    public void generateAndSendTokens(HttpServletResponse response, Authentication authentication) throws IOException {
        // JWT 생성
        Map<String, Object> claim = Map.of(
                "username", authentication.getName(),
                "authority", authentication.getAuthorities()
        );
        // 액세스 토큰 5분
        String accessToken = jwtProvider.generateToken(claim, 10);
        // 리프레시 토큰 10분
        String refreshToken = jwtProvider.generateToken(claim, 60);

        // 액세스 토큰 쿠키 생성
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(60 * 15) // 15분
                .domain("moviepunk.o-r.kr")
                .sameSite("Lax")
                .build();

        // 리프레시 토큰 쿠키 생성
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(60 * 90) // 90분
                .domain("moviepunk.o-r.kr")
                .sameSite("Lax")
                .build();

        // 쿠키를 응답에 추가
        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        String jsonStr = gson.toJson(claim);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().println(jsonStr);
    }
}
