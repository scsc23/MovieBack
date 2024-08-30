package org.movieproject.config.Filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.movieproject.security.exception.RefreshTokenException;
import org.movieproject.security.JwtProvider;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Log4j2
@RequiredArgsConstructor
public class RefreshTokenFilter extends OncePerRequestFilter {

    private final String refreshPath;

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (!path.equals(refreshPath)) {
            log.info("요청 패쓰 : " + path);
            log.info("리프레시 요청 X");
            filterChain.doFilter(request, response);
            return;
        }
        log.info("리 프 레 시 토 큰 필 터 실 행");

        // 쿠키에서 accessToken 과 refreshToken 가져오기
        Cookie accessTokenCookie = WebUtils.getCookie(request, "accessToken");
        Cookie refreshTokenCookie = WebUtils.getCookie(request, "refreshToken");

        if (accessTokenCookie == null || refreshTokenCookie == null) {
            log.info("No access or refresh token present in cookies");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access token or refresh token is missing");
            return;
        }

        String accessToken = accessTokenCookie.getValue();
        String refreshToken = refreshTokenCookie.getValue();

        log.info("액세스 토큰 : " + accessToken);
        log.info("리프레시 토큰 : " + refreshToken);

        boolean accessTokenValid = true;
        try {
            checkAccessToken(accessToken);
        } catch (RefreshTokenException refreshTokenException) {
            accessTokenValid = false;
        }

        Map<String, Object> refreshClaims = null;

        try {
            refreshClaims = checkRefreshToken(refreshToken);
            log.info("리프레시클레임"+refreshClaims);

            // refreshToken 의 유효기간이 얼마 남지 않은 경우
            long exp = (long) refreshClaims.get("exp");

            Date expTime = new Date(Instant.ofEpochMilli(exp).toEpochMilli() * 1000);

            Date current = new Date(System.currentTimeMillis());

            // 만료 시간과 현재 시간의 간격 계산
            // 5분 미만이면 refreshToken 생성
            long gapTime = (expTime.getTime() - current.getTime());

            long seconds = (gapTime / 1000) % 60;
            long minutes = (gapTime / (1000 * 60)) % 60;
            long hours = (gapTime / (1000 * 60 * 60)) % 24;

            log.info("현 재 시 간 : " + current);
            log.info("만 료 시 간 : " + expTime);
            log.info("갭 타 임 : " + hours + "시간 " + minutes + "분 " + seconds + "초");

            String username = (String) refreshClaims.get("username");
            log.info("리프레시유저네임 " +username);
            Map<String, Object> authority = Map.of("authority", refreshClaims.get("authority"));
            log.info("리프레시인증 " + authority);
            // 여기부터 AccessToken 생성
            String accessTokenValue;
            if (accessTokenValid) {
                accessTokenValue = accessToken;
            } else {
                accessTokenValue = jwtProvider.generateToken(Map.of("username", username, "authority", authority), 10);
            }

            String refreshTokenValue = refreshToken;

            // refreshToken 만료 시간이 5분 이하일 때
            if (gapTime < (1000 * 60 * 5)) {
                log.info("리프레시 토큰을 새로 만듭시다");
                refreshTokenValue = jwtProvider.generateToken(Map.of("username", username, "authority", authority), 60);
            }

            log.info("액 세 스 토 큰 : " + accessTokenValue);
            log.info("리 프 레 시 토 큰 : " + refreshTokenValue);

            sendTokens(accessTokenValue, refreshTokenValue, response);
        } catch (RefreshTokenException refreshTokenException) {
            refreshTokenException.sendResponseError(response);
        }
    }

    private void checkAccessToken(String accessToken) throws RefreshTokenException {
        try {
            jwtProvider.extractClaim(accessToken);
        } catch (ExpiredJwtException expiredJwtException) {
            log.info("액 세 스 토 큰 이 만 료 되 었 다");
            throw new RefreshTokenException(RefreshTokenException.ErrorCase.NO_ACCESS); // RefreshTokenException을 던짐
        } catch (Exception exception) {
            throw new RefreshTokenException(RefreshTokenException.ErrorCase.NO_ACCESS);
        }
    }

    private Map<String, Object> checkRefreshToken(String refreshToken) throws RefreshTokenException {
        try {
            Map<String, Object> values = jwtProvider.extractClaim(refreshToken);
            return values;
        } catch (ExpiredJwtException expiredJwtException) {
            throw new RefreshTokenException(RefreshTokenException.ErrorCase.OLD_REFRESH);
        } catch (MalformedJwtException malformedJwtException) {
            log.info("말 폼 드 J W T 익 셉 션");
            throw new RefreshTokenException(RefreshTokenException.ErrorCase.NO_REFRESH);
        } catch (Exception exception) {
            throw new RefreshTokenException(RefreshTokenException.ErrorCase.NO_REFRESH);
        }
    }

    private void sendTokens(String accessTokenValue, String refreshTokenValue, HttpServletResponse response) {

        Cookie accessTokenCookie = new Cookie("accessToken", accessTokenValue);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(60*15); // 15분

        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshTokenValue);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(60*90); // 90분

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        try {
            response.getWriter().println("{\"message\":\"Tokens refreshed successfully\"}");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
