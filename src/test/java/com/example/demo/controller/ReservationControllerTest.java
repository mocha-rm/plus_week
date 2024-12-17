package com.example.demo.controller;

import com.example.demo.dto.ReservationResponseDto;
import com.example.demo.entity.Item;
import com.example.demo.entity.Reservation;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.service.ReservationService;
import com.example.demo.status.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ReservationController.class)
class ReservationControllerTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters() // 특정 필터를 추가하거나 제외
                .build();

        startAt = LocalDateTime.parse("2024-12-18T14:30:11");
        endAt = LocalDateTime.parse("2024-12-18T14:30:11");
    }

    @Test
    @DisplayName("예약 생성 테스트")
    void createReservationTest() throws Exception {
        // Given
        User user = createTestUser();
        Item item = createTestItem(user);
        Reservation reservation = createTestReservation(item, user, startAt, endAt, ReservationStatus.PENDING);
        ReservationResponseDto reservationResponseDto = new ReservationResponseDto(reservation.getId(), reservation.getUser().getNickname(), reservation.getItem().getName(), reservation.getStatus(), reservation.getStartAt(), reservation.getEndAt());

        given(reservationService.createReservation(item.getId(), user.getId(), startAt, endAt)).willReturn(reservationResponseDto);

        // When
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createReservationJson(item.getId(), user.getId(), startAt, endAt)))
                .andExpect(status().isCreated()) // Then
                .andExpect(jsonPath("$.id").value(reservation.getId()))
                .andExpect(jsonPath("$.nickname").value(reservation.getUser().getNickname()))
                .andExpect(jsonPath("$.itemName").value(reservation.getItem().getName()))
                .andExpect(jsonPath("$.status").value(reservation.getStatus().toString()))
                .andExpect(jsonPath("$.startAt").value(startAt.toString()))
                .andExpect(jsonPath("$.endAt").value(endAt.toString()));
    }

    @Test
    @DisplayName("예약 생성 테스트 (아이템이 없는 경우)")
    void createReservationWithoutItemTest() throws Exception {
        // Given
        User user = createTestUser();
        given(reservationService.createReservation(99L, user.getId(), startAt, endAt))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 ID에 맞는 값이 존재하지 않습니다."));

        // When
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createReservationJson(99L, user.getId(), startAt, endAt)))
                .andExpect(status().isNotFound()) // Then
                .andExpect(result -> assertTrue(
                        Objects.requireNonNull(result.getResolvedException()).getMessage().contains("해당 ID에 맞는 값이 존재하지 않습니다.")
                ));
    }

    @Test
    @DisplayName("예약 생성 테스트 (유저가 없는 경우)")
    void createReservationWithoutUserTest() throws Exception {
        // Given
        Item item = createTestItem(createTestUser());
        given(reservationService.createReservation(item.getId(), 99L, startAt, endAt))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 ID에 맞는 값이 존재하지 않습니다."));

        // When
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createReservationJson(item.getId(), 99L, startAt, endAt)))
                .andExpect(status().isNotFound()) // Then
                .andExpect(result -> assertTrue(
                        Objects.requireNonNull(result.getResolvedException()).getMessage().contains("해당 ID에 맞는 값이 존재하지 않습니다.")
                ));
    }

    @Test
    @DisplayName("예약 상태 변경 - 정상 상태 전환")
    void patchReservationStatusTest() throws Exception {
        // Given
        User testUser = createTestUser();
        Item testItem = createTestItem(testUser);
        Reservation testReservation = createTestReservation(testItem, testUser, startAt, endAt, ReservationStatus.PENDING); //초기 상태

        ReservationStatus status = ReservationStatus.APPROVED; //바꿀 상태

        // Mock 설정: 정상 동작
        given(reservationService.updateReservationStatus(testReservation.getId(), status))
                .willReturn(new ReservationResponseDto(testReservation.getId(),
                        testReservation.getUser().getNickname(),
                        testReservation.getItem().getName(),
                        testReservation.getStatus(),
                        testReservation.getStartAt(),
                        testReservation.getEndAt()));

        // When
        mockMvc.perform(patch("/reservations/{id}/update-status", testReservation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchReservationJson(status)))
                .andExpect(status().isOk()) // Then
                .andExpect(jsonPath("$.id").value(testReservation.getId()))
                .andExpect(jsonPath("$.nickname").value(testReservation.getUser().getNickname()))
                .andExpect(jsonPath("$.itemName").value(testReservation.getItem().getName()))
                .andExpect(jsonPath("$.status").value(testReservation.getStatus().toString()))
                .andExpect(jsonPath("$.startAt").value(startAt.toString()))
                .andExpect(jsonPath("$.endAt").value(endAt.toString()));
    }

    @Test
    @DisplayName("예약 상태 변경 - 예외 발생 (EXPIRED 상태에서 변경 시도)")
    void patchReservationStatusExpiredToCanceledTest() throws Exception {
        // Given
        User testUser = createTestUser();
        Item testItem = createTestItem(testUser);
        Reservation testReservation = createTestReservation(testItem, testUser, startAt, endAt, ReservationStatus.EXPIRED); // 초기 상태

        ReservationStatus status = ReservationStatus.CANCELED; // 바꿀 상태

        // Mock 설정: 예외 발생
        given(reservationService.updateReservationStatus(testReservation.getId(), status))
                .willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "EXPIRED 상태인 예약은 취소할 수 없습니다."));

        // When
        mockMvc.perform(patch("/reservations/{id}/update-status", testReservation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchReservationJson(status)))
                .andExpect(status().isBadRequest()) // Then
                .andExpect(result -> assertTrue(
                        Objects.requireNonNull(result.getResolvedException()).getMessage().contains("EXPIRED 상태인 예약은 취소할 수 없습니다.")
                ));
    }

    @Test
    @DisplayName("예약 상태 변경 - 예외 발생 (PENDING 상태가 아닌 경우에 변경 시도)")
    void patchReservationStatusInvalidStateTest() throws Exception {
        // Given
        User testUser = createTestUser();
        Item testItem = createTestItem(testUser);
        Reservation testReservation = createTestReservation(testItem, testUser, startAt, endAt, ReservationStatus.CANCELED); // 초기 상태

        ReservationStatus status = ReservationStatus.APPROVED; // 바꿀 상태

        // Mock 설정: 예외 발생
        given(reservationService.updateReservationStatus(testReservation.getId(), status))
                .willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "PENDING 상태만 " + status + "로 변경 가능합니다."));

        // When
        mockMvc.perform(patch("/reservations/{id}/update-status", testReservation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchReservationJson(status)))
                .andExpect(status().isBadRequest()) // Then
                .andExpect(result -> assertTrue(
                        Objects.requireNonNull(result.getResolvedException()).getMessage().contains("PENDING 상태만 " + status + "로 변경 가능합니다.")
                ));
    }

    private User createTestUser() {
        return new User(1L, "user@gmail.com", "user", "1234", "NORMAL", Role.USER);
    }

    private Item createTestItem(User user) {
        return new Item(1L, "testItem", "description", user, user, "PENDING");
    }

    private Reservation createTestReservation(Item item, User user, LocalDateTime startAt, LocalDateTime endAt, ReservationStatus reservationStatus) {
        return new Reservation(1L, item, user, startAt, endAt, reservationStatus);
    }

    private String createReservationJson(Long itemId, Long userId, LocalDateTime startAt, LocalDateTime endAt) {
        return """
                {
                    "itemId": %d,
                    "userId": %d,
                    "startAt": "%s",
                    "endAt": "%s"
                }
                """.formatted(itemId, userId, startAt.toString(), endAt.toString());
    }

    private String patchReservationJson(ReservationStatus reservationStatus) {
        return """
                {
                    "status": "%s"
                }
                """.formatted(reservationStatus.toString());
    }
}