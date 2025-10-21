package com.example.purchasedemo.util;

import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        // 테스트용 시크릿 키는 실제 키와 길이가 같아야 합니다. (HS256은 256비트, 즉 32바이트 이상)
        String secretKey = "testsecrettestsecrettestsecrettestsecrettestsecret";
        jwtTokenProvider = new JwtTokenProvider(secretKey);
    }

    @Test
    @DisplayName("토큰 생성 및 검증 성공")
    void createToken_and_validateToken() {
        // given
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser",
                null,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));

        // when
        String token = jwtTokenProvider.createToken(authentication);

        // then
        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        Authentication resultAuth = jwtTokenProvider.getAuthentication(token);
        assertThat(resultAuth.getName()).isEqualTo("testuser");
        assertThat(resultAuth.getAuthorities())
                .extracting("authority")
                .contains("ROLE_USER");
    }

    @Test
    @DisplayName("유효하지 않은 토큰 검증 시 실패")
    void validateToken_fail_withInvalidToken() {
        // given
        String invalidToken = "invalid.token.value";

        // when
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // then
        assertThat(isValid).isFalse();
    }
}
