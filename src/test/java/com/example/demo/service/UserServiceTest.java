package com.example.demo.service;

import com.example.demo.dto.Authentication;
import com.example.demo.dto.LoginRequestDto;
import com.example.demo.dto.UserRequestDto;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.PasswordEncoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService userService;

    @Test
    @DisplayName("회원가입 - 유저")
    void userSignUpTest() {
        // Given
        String role = "user";
        String email = "user@mail.com";
        String nickname = "user";
        String password = "test!1234";

        UserRequestDto userRequestDto = new UserRequestDto(role, email, nickname, password); // DTO 입력
        String encodedPassword = PasswordEncoder.encode(userRequestDto.getPassword()); // 패스워드 암호화
        userRequestDto.updatePassword(encodedPassword); // 패스워드 업데이트

        User mockUser = userRequestDto.toEntity();

        // When

        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        userService.signupWithEmail(userRequestDto);
        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(mockUser);
        User findUser = userRepository.findByEmail(mockUser.getEmail());

        // Then

        assertThat(findUser.getRole()).isEqualTo(mockUser.getRole());
        assertThat(findUser.getEmail()).isEqualTo(mockUser.getEmail());
        assertThat(findUser.getNickname()).isEqualTo(mockUser.getNickname());
        assertThat(findUser.getPassword()).isEqualTo(mockUser.getPassword());
    }

    @Test
    @DisplayName("회원가입 - 관리자")
    void adminSignUpTest() {
        // Given
        String role = "admin";
        String email = "admin@mail.com";
        String nickname = "admin";
        String password = "test!1234";

        UserRequestDto userRequestDto = new UserRequestDto(role, email, nickname, password); // DTO 입력
        String encodedPassword = PasswordEncoder.encode(userRequestDto.getPassword()); // 패스워드 암호화
        userRequestDto.updatePassword(encodedPassword); // 패스워드 업데이트

        User mockUser = userRequestDto.toEntity();

        // When

        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        userService.signupWithEmail(userRequestDto);
        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(mockUser);
        User findUser = userRepository.findByEmail(mockUser.getEmail());

        // Then

        assertThat(findUser.getRole()).isEqualTo(mockUser.getRole());
        assertThat(findUser.getEmail()).isEqualTo(mockUser.getEmail());
        assertThat(findUser.getNickname()).isEqualTo(mockUser.getNickname());
        assertThat(findUser.getPassword()).isEqualTo(mockUser.getPassword());
    }

    @Test
    @DisplayName("로그인")
    void loginUser() {
        // Given
        String email = "user@mail.com";
        String password = "1234";
        String encodedPassword = PasswordEncoder.encode(password);

        LoginRequestDto loginRequestDto = new LoginRequestDto(email, password);
        User mockUser = new User("user", loginRequestDto.getEmail(), "user", encodedPassword);

        // When
        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(mockUser);
        Authentication authentication = userService.loginUser(loginRequestDto);

        // Then
        assertThat(authentication.getId()).isEqualTo(mockUser.getId());
        assertThat(authentication.getRole()).isEqualTo(mockUser.getRole());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void loginFailCauseOfPassword() {
        // Given
        String email = "user@mail.com";
        String password = "1234";
        String encodedPassword = PasswordEncoder.encode(password);

        LoginRequestDto loginRequestDto = new LoginRequestDto(email, password);
        User mockUser = new User("user", loginRequestDto.getEmail(), "user", "wrongPassword");

        // When
        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(mockUser);
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.loginUser(loginRequestDto)
        );

        // Then
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getMessage()).contains("유효하지 않은 사용자 이름 혹은 잘못된 비밀번호");
    }

    @Test
    @DisplayName("로그인 실패 - 없는 유저")
    void loginFailCauseOfUserNotFound() {
        // Given
        String email = "user@mail.com";
        String password = "1234";
        String encodedPassword = PasswordEncoder.encode(password);

        LoginRequestDto loginRequestDto = new LoginRequestDto(email, password);
        User mockUser = new User("user", loginRequestDto.getEmail(), "user", encodedPassword);

        // When
        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(null);
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.loginUser(loginRequestDto)
        );

        // Then
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getMessage()).contains("유효하지 않은 사용자 이름 혹은 잘못된 비밀번호");
    }
}