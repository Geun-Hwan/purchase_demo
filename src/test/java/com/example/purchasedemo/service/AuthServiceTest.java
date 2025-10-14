package com.example.purchasedemo.service;

import com.example.purchasedemo.dto.LoginRequest;
import com.example.purchasedemo.dto.SignupRequest;
import com.example.purchasedemo.entity.User;
import com.example.purchasedemo.repository.UserRepository;
import com.example.purchasedemo.util.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("회원가입 성공")
    void registerUser_success() {
        // given
        SignupRequest request = new SignupRequest();
        request.setUsername("newuser");
        request.setPassword("password");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(User.builder().build());

        // when
        authService.registerUser(request);

        // then: No exception thrown
    }

    @Test
    @DisplayName("이미 존재하는 아이디로 회원가입 시 예외 발생")
    void registerUser_fail_whenUsernameIsTaken() {
        // given
        SignupRequest request = new SignupRequest();
        request.setUsername("existinguser");
        request.setPassword("password");

        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(User.builder().build()));

        // when & then
        assertThatThrownBy(() -> authService.registerUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username is already taken");
    }

    @Test
    @DisplayName("로그인 성공")
    void authenticateUser_success() {
        // given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        when(jwtTokenProvider.createToken(any())).thenReturn("test.token");

        // when
        Map<String, String> tokenMap = authService.authenticateUser(request);

        // then
        assertThat(tokenMap).isNotNull();
        assertThat(tokenMap.get("token")).isEqualTo("test.token");
    }
}
