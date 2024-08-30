package org.movieproject.config.Filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.movieproject.security.exception.AccessTokenException;
import org.movieproject.security.JwtProvider;
import org.movieproject.security.MvpUserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Log4j2
@RequiredArgsConstructor
public class TokenCheckFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final MvpUserDetailsService mvpUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        log.info("요청 URI : " + path);

        // 특정 경로에 대해서만 필터 적용
        if (!path.startsWith("/api/member") || path.equals("/api/member/join") || path.equals("/api/member/logout")
                ||path.equals("/api/member/delete/**")) {
            filterChain.doFilter(request, response);
            return;
        }
        log.info("토큰 체크 필터");

        try {
            Map<String, Object> payload = validateAccessToken(request);
            // username 값 얻기
            String username = (String) payload.get("username");

            // UserDetail 정보 얻기
            UserDetails userDetails = mvpUserDetailsService.loadUserByUsername(username);

            // 등록 사용자 인증 정보 생성
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // Spring Security 에 인증 정보 등록
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            filterChain.doFilter(request, response);

        } catch (AccessTokenException accessTokenException) {
            accessTokenException.sendResponseError(response);
        }
    }

    private Map<String, Object> validateAccessToken(HttpServletRequest request) throws AccessTokenException {

        String accessToken = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    accessToken = cookie.getValue();
                    break;
                }
            }
        }

        if (accessToken == null || accessToken.isEmpty()) {
            log.info("액세스 토큰이 존재하지 않거나 비어 있습니다.");
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.UNACCEPT);
        }

        log.info("액세스토큰 쿠키 : " + accessToken);

        try {
            // 블랙리스트 검증
            if (jwtProvider.isBlacklisted(accessToken)) {
                log.info("블랙리스트에 등록된 토큰입니다.");
                throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.BLACKLISTED);
            }
            Map<String, Object> values = jwtProvider.extractClaim(accessToken);
            return values;
        } catch (MalformedJwtException malformedJwtException) {
            log.info("말 폼 드 JWT 익셉션");
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.MALFORM);
        } catch (SignatureException signatureException) {
            log.info("시그니처 익셉션");
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.BADSIGN);
        } catch (ExpiredJwtException expiredJwtException) {
            log.info("만료된 토큰 익셉션");
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.EXPIRED);
        }
    }
}
