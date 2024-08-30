package org.movieproject.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.movieproject.config.Filter.APILoginFilter;
import org.movieproject.config.Filter.RefreshTokenFilter;
import org.movieproject.config.Filter.TokenCheckFilter;
import org.movieproject.config.handler.APILoginFailureHandler;
import org.movieproject.config.handler.APILoginSuccessHandler;
import org.movieproject.config.handler.Custom403Handler;
import org.movieproject.config.handler.MvpSocialLoginSuccessHandler;
import org.movieproject.security.JwtProvider;
import org.movieproject.security.MvpOIDCUserService;
import org.movieproject.security.MvpOauth2UserService;
import org.movieproject.security.MvpUserDetailsService;
import org.movieproject.security.util.JwtLoginUtil;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.sql.DataSource;
import java.util.List;

@Log4j2
@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
public class CustomSecurityConfig {

    private final DataSource dataSource;
    private final MvpUserDetailsService mvpUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final JwtLoginUtil jwtLoginUtil;
    private final MvpOauth2UserService mvpOauth2UserService;
    private final MvpOIDCUserService mvpOIDCUserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        log.info("--------------------configure-------------------");

        http
            .cors(cors -> { // CORS 설정
                CorsConfigurationSource source = corsConfigurationSource();
                cors.configurationSource(source);
            })
            .csrf(AbstractHttpConfigurer::disable)  // CSRF 비활성화
            .sessionManagement(httpSecuritySessionManagementConfigurer ->   // 세션 비활성화
                    httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize // 권한 설정
                    .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                    .anyRequest().permitAll()
                    )
            .formLogin(AbstractHttpConfigurer::disable) // 기본 로그인폼 비활성화
            .oauth2Login(httpSecurityOauth2LoginConfigurer -> { // 소셜 로그인 설정
                httpSecurityOauth2LoginConfigurer.loginPage("/member/login")
                        .userInfoEndpoint(userInfoEndpointConfigurer -> {
                            userInfoEndpointConfigurer
                                    .userService(mvpOauth2UserService) // OAuth2 user service for non-OIDC providers
                                    .oidcUserService(mvpOIDCUserService); // OIDC user service for OIDC providers
                        })
                        .successHandler(mvpSocialLoginSuccessHandler());

            })
            .logout(AbstractHttpConfigurer::disable);   // 기본 로그아웃 비활성화


        // 인 증 매 니 저 설 정
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(mvpUserDetailsService)
                .passwordEncoder(passwordEncoder);

        // 매 니 저 가 져 오 기
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();
        // 인 증 매 니 저 등 록
        http.authenticationManager(authenticationManager);

        // 로그인 필터 설정
        APILoginFilter apiLoginFilter = new APILoginFilter("/api/login");
        apiLoginFilter.setAuthenticationManager(authenticationManager);
        // 로그인 성공 핸들러
        APILoginSuccessHandler successHandler = new APILoginSuccessHandler(jwtLoginUtil);
        apiLoginFilter.setAuthenticationSuccessHandler(successHandler);
        // 로그인 실패 핸들러
        APILoginFailureHandler failureHandler = new APILoginFailureHandler();
        apiLoginFilter.setAuthenticationFailureHandler(failureHandler);

        // 필터 위치 조정
        // 로그인 필터
        http.addFilterBefore(apiLoginFilter, UsernamePasswordAuthenticationFilter.class);
        // 토큰 체크 필터
        http.addFilterBefore(tokenCheckFilter(jwtProvider, mvpUserDetailsService), UsernamePasswordAuthenticationFilter.class);
        // 리프레시 토큰 필터
        http.addFilterBefore(new RefreshTokenFilter("/api/member/check_auth/refresh", jwtProvider), TokenCheckFilter.class);

        http.exceptionHandling(httpSecurityExceptionHandlingConfigurer -> {
            httpSecurityExceptionHandlingConfigurer.accessDeniedHandler(accessDeniedHandler());
        });

        return http.build();
    }

    @Bean   // 자동로그인 관련
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl repo = new JdbcTokenRepositoryImpl();
        repo.setDataSource(dataSource);

        return repo;
    }

    @Bean   // 정적 리소스 필터링 제외
    public WebSecurityCustomizer webSecurityCustomizer() {
        log.info("-------------------- web configure  -------------------");

        return (web -> web.ignoring().requestMatchers(PathRequest.toStaticResources()
                .atCommonLocations()));
    }

    @Bean   // 엑세스 디나이드 핸들러
    public AccessDeniedHandler accessDeniedHandler() {
        return new Custom403Handler();
    }

    @Bean
    public AuthenticationSuccessHandler apiLoginSuccessHandler() {
        return new APILoginSuccessHandler(jwtLoginUtil);
    }

    @Bean
    public AuthenticationSuccessHandler mvpSocialLoginSuccessHandler() {
        return new MvpSocialLoginSuccessHandler(jwtLoginUtil);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 허용할 도메인만 지정
        configuration.setAllowedOrigins(List.of(
                "http://moviefront-env.eba-r8jmajf2.ap-northeast-2.elasticbeanstalk.com/"
        ));

        // 허용할 HTTP 메서드만 지정
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));

        // 허용할 헤더만 지정
        configuration.setAllowedHeaders(List.of("*"));

        // 클라이언트에 노출할 헤더 지정
        configuration.setExposedHeaders(List.of("*"));

        // 자격 증명 허용
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // Token Check Filter 생성
    private TokenCheckFilter tokenCheckFilter(JwtProvider jwtProvider, MvpUserDetailsService mvpUserDetailsService) {
        return new TokenCheckFilter(jwtProvider, mvpUserDetailsService);
    }
}
