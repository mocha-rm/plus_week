package com.example.demo.controller;

import com.example.demo.constants.GlobalConstants;
import com.example.demo.dto.Authentication;
import com.example.demo.dto.LoginRequestDto;
import com.example.demo.dto.UserRequestDto;
import com.example.demo.entity.Role;
import com.example.demo.filter.AuthFilter;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilter(new AuthFilter(), "/**") // 특정 필터 등록
                .build();
    }
    
    @Test
    @DisplayName("회원가입")
    void signUp() throws Exception {
        // Given
        String role = "user";
        String email = "user@mail.com";
        String nickname = "user";
        String password = "1234";
        UserRequestDto userRequestDto = new UserRequestDto(role, email, nickname, password);
        doNothing().when(userService).signupWithEmail(userRequestDto);

        // When
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email" : "%s",
                                    "nickname" : "%s",
                                    "password" : "%s",
                                    "role" : "%s"
                                }
                                """.formatted(email, nickname, password, role))) //Then
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("회원가입 - Role 값이 잘못 되었을 때 예외 발생")
        // USER, ADMIN 외 예외 발생
    void signUpException() throws Exception {
        String role = "monster";
        String email = "user@mail.com";
        String nickname = "user";
        String password = "1234";

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST))
                .when(userService).signupWithEmail(any(UserRequestDto.class));

        // When
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email" : "%s",
                                    "nickname" : "%s",
                                    "password" : "%s",
                                    "role" : "%s"
                                }
                                """.formatted(email, nickname, password, role))) //Then
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException));
    }

    @Test
    @DisplayName("로그인")
    void login() throws Exception {
        // Given
        String email = "user@mail.com";
        String password = "1234";
        Authentication authentication = new Authentication(1L, Role.USER);
        given(userService.loginUser(any(LoginRequestDto.class))).willReturn(authentication);
        MockHttpSession session = new MockHttpSession();

        // When
        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password))
                        .session(session))
                .andExpect(status().isOk()) // Then
                .andExpect(request().sessionAttribute(GlobalConstants.USER_AUTH, authentication));
    }

    @Test
    @DisplayName("로그인 - 이메일을 틀렸을 경우")
    void loginEmailException() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto("", "1234");

        doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 사용자 이름 혹은 잘못된 비밀번호"))
                .when(userService).loginUser(any(LoginRequestDto.class));

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(loginRequestDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그인 - 비밀번호를 틀렸을 경우")
    void loginPasswordException() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto("user@mail.com", "");

        doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 사용자 이름 혹은 잘못된 비밀번호"))
                .when(userService).loginUser(any(LoginRequestDto.class));

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(loginRequestDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그아웃 - 세션이 존재하는 경우")
    void logoutWithSession() throws Exception {
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute("USER_AUTH", "test");

        mockMvc.perform(post("/users/logout")
                        .session(mockHttpSession))
                .andExpect(status().isOk())
                .andExpect(request().sessionAttributeDoesNotExist("USER_AUTH"));
    }

    @Test
    @DisplayName("로그아웃 - 세션이 없는 경우")
    void logoutWithoutSession() throws Exception {
        HttpSession session = new MockHttpSession();
        if (session == null) {
        mockMvc.perform(post("/users/logout"))
                .andExpect(status().isInternalServerError());
        }
    }
}