package com.example.demo.service;

import com.example.demo.dto.ReservationResponseDto;
import com.example.demo.entity.Item;
import com.example.demo.entity.Reservation;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.ReservationConflictException;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.status.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    @BeforeEach
    void setUp() {
        startAt = LocalDateTime.now();
        endAt = LocalDateTime.now();
    }

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RentalLogService rentalLogService;

    @InjectMocks
    private ReservationService reservationService;


    @Test
    @DisplayName("예약 생성 (성공)")
    void createReservationTest() {
        // Given
        User mockUser = createMockUser();
        Item mockItem = createMockItem(mockUser);

        when(userRepository.findById(mockUser.getId())).thenReturn(Optional.of(mockUser));
        when(itemRepository.findById(mockItem.getId())).thenReturn(Optional.of(mockItem));

        Reservation savedReservation = new Reservation(mockItem, mockUser, ReservationStatus.PENDING, startAt, endAt);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);

        // When
        ReservationResponseDto reservation = reservationService.createReservation(mockItem.getId(), mockUser.getId(), startAt, endAt);

        // Then
        assertThat(reservation).isNotNull();
        assertThat(reservation.getNickname()).isEqualTo(mockUser.getNickname());
        assertThat(reservation.getItemName()).isEqualTo(mockItem.getName());
    }

    @Test
    @DisplayName("예약 생성 실패 - 존재하지 않는 User일 경우 예외 발생")
    void createReservationWithInvalidUserTest() {
        // Given
        Long invalidUserId = 999L;
        Long validItemId = 1L;
        Item mockItem = createMockItem(createMockUser());

        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());
        when(itemRepository.findById(validItemId)).thenReturn(Optional.of(mockItem));

        // When
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reservationService.createReservation(validItemId, invalidUserId, startAt, endAt)
        );

        // Then
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getMessage()).contains("해당 ID에 맞는 값이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("예약 생성 실패 - 존재하지 않는 Item일 경우 예외 발생")
    void createReservationWithInvalidItemTest() {
        // Given
        Long validUserId = 1L;
        Long invalidItemId = 999L;

        when(itemRepository.findById(invalidItemId)).thenReturn(Optional.empty());

        // When
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reservationService.createReservation(invalidItemId, validUserId, startAt, endAt)
        );

        // Then
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getMessage()).contains("해당 ID에 맞는 값이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("예약 생성 실패 - 예약 시간이 중복될 경우 예외 발생")
    void createReservationWithConflictingTimeTest() {
        // Given
        User mockUser = createMockUser();
        Item mockItem = createMockItem(mockUser);

        when(reservationRepository.findConflictingReservations(mockItem.getId(), startAt, endAt))
                .thenReturn(List.of(new Reservation(mockItem, mockUser, ReservationStatus.PENDING, startAt, endAt)));

        // When
        ReservationConflictException exception = assertThrows(
                ReservationConflictException.class,
                () -> reservationService.createReservation(mockItem.getId(), mockUser.getId(), startAt, endAt)
        );

        // Then
        assertThat(exception.getMessage()).isEqualTo("해당 물건은 이미 그 시간에 예약이 있습니다.");
    }

    @Test
    @DisplayName("예약 목록 조회")
    void getReservationsTest() {
        // Given
        User mockUser = createMockUser();
        Reservation mockReservation = createMockReservation(createMockItem(mockUser), mockUser, startAt, endAt);
        List<Reservation> mockReservations = List.of(mockReservation);
        when(reservationRepository.findAllWithItemsAndUsers()).thenReturn(mockReservations);

        // When
        List<ReservationResponseDto> result = reservationService.getReservations();

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(1);

        ReservationResponseDto responseDto = result.get(0);
        assertThat(responseDto.getId()).isEqualTo(mockReservation.getId());
        assertThat(responseDto.getNickname()).isEqualTo(mockReservation.getUser().getNickname());
        assertThat(responseDto.getItemName()).isEqualTo(mockReservation.getItem().getName());
        assertThat(responseDto.getStatus()).isEqualTo(mockReservation.getStatus());
        assertThat(responseDto.getStartAt()).isEqualTo(mockReservation.getStartAt());
        assertThat(responseDto.getEndAt()).isEqualTo(mockReservation.getEndAt());
    }

    @Test
    void searchAndConvertReservationsTest() {
        // Given
        Long mockUserId = 1L;
        Long mockItemId = 1L;

        ReservationResponseDto mockResponseDto = new ReservationResponseDto(
                1L,
                "user",
                "testItem",
                ReservationStatus.PENDING,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        List<ReservationResponseDto> mockSearchResults = List.of(mockResponseDto);

        when(reservationRepository.searchReservations(mockUserId, mockItemId)).thenReturn(mockSearchResults);
//        when(reservationRepository.searchReservations(null, mockItemId)).thenReturn(mockSearchResults);
//        when(reservationRepository.searchReservations(mockUserId, null)).thenReturn(mockSearchResults);
//        when(reservationRepository.searchReservations(null, null)).thenReturn(mockSearchResults);

        // When
        List<ReservationResponseDto> result = reservationService.searchAndConvertReservations(mockUserId, mockItemId);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(1);

        ReservationResponseDto responseDto = result.get(0);
        assertThat(responseDto.getId()).isEqualTo(mockResponseDto.getId());
        assertThat(responseDto.getNickname()).isEqualTo(mockResponseDto.getNickname());
        assertThat(responseDto.getItemName()).isEqualTo(mockResponseDto.getItemName());
        assertThat(responseDto.getStatus()).isEqualTo(mockResponseDto.getStatus());
        assertThat(responseDto.getStartAt()).isEqualTo(mockResponseDto.getStartAt());
        assertThat(responseDto.getEndAt()).isEqualTo(mockResponseDto.getEndAt());
    }

    @Test
    @DisplayName("예약 상태를 PENDING에서 APPROVED로 변경")
    void updateReservationStatusToApprovedTest() {
        // Given
        Long reservationId = 1L;
        User mockUser = createMockUser();
        Item mockItem = createMockItem(mockUser);
        Reservation mockReservation = new Reservation(
                mockItem,
                mockUser, ReservationStatus.PENDING, LocalDateTime.now(), LocalDateTime.now().plusHours(2));

        when(reservationRepository.findByIdWithQuery(reservationId)).thenReturn(mockReservation);

        // When
        ReservationResponseDto result = reservationService.updateReservationStatus(reservationId, ReservationStatus.APPROVED);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.APPROVED);
    }

    @Test
    @DisplayName("EXPIRED 상태의 예약을 CANCELED로 변경하려고 하면 예외 발생")
    void updateReservationStatusToCanceledFromExpiredTest() {
        // Given
        Long reservationId = 1L;
        User mockUser = createMockUser();
        Item mockItem = createMockItem(mockUser);
        Reservation mockReservation = new Reservation(
                mockItem,
                mockUser, ReservationStatus.EXPIRED, LocalDateTime.now(), LocalDateTime.now().plusHours(2));

        when(reservationRepository.findByIdWithQuery(reservationId)).thenReturn(mockReservation);

        // When
        assertThatThrownBy(() -> reservationService.updateReservationStatus(reservationId, ReservationStatus.CANCELED))
                .isInstanceOf(ResponseStatusException.class) // Then
                .hasMessageContaining("EXPIRED 상태인 예약은 취소할 수 없습니다.");
    }

    @Test
    @DisplayName("PENDING이 아닌 상태를 APPROVED로 변경하려고 하면 예외 발생")
    void updateReservationStatusToApprovedFromNonPendingTest() {
        // Given
        Long reservationId = 1L;
        User mockUser = createMockUser();
        Item mockItem = createMockItem(mockUser);
        Reservation mockReservation = new Reservation(
                mockItem,
                mockUser, ReservationStatus.CANCELED, LocalDateTime.now(), LocalDateTime.now().plusHours(2));

        when(reservationRepository.findByIdWithQuery(reservationId)).thenReturn(mockReservation);

        // When
        assertThatThrownBy(() -> reservationService.updateReservationStatus(reservationId, ReservationStatus.APPROVED))
                .isInstanceOf(ResponseStatusException.class) // Then
                .hasMessageContaining("PENDING 상태만 APPROVED로 변경 가능합니다.");
    }

    @Test
    @DisplayName("지원되지 않는 상태를 설정하려고 하면 예외 발생")
    void updateReservationStatusToInvalidStateTest() {
        // Given
        Long reservationId = 1L;
        User mockUser = createMockUser();
        Item mockItem = createMockItem(mockUser);
        Reservation mockReservation = new Reservation(
                mockItem,
                mockUser, ReservationStatus.PENDING, LocalDateTime.now(), LocalDateTime.now().plusHours(2));

        when(reservationRepository.findByIdWithQuery(reservationId)).thenReturn(mockReservation);

        // When
        assertThatThrownBy(() -> reservationService.updateReservationStatus(reservationId, ReservationStatus.PENDING))
                .isInstanceOf(ResponseStatusException.class) // Then
                .hasMessageContaining("올바르지 않은 상태: " + mockReservation.getStatus());
    }


    private User createMockUser() {
        return new User(1L, "user@gmail.com", "user", "1234", "NORMAL", Role.USER);
    }

    private Item createMockItem(User user) {
        return new Item(1L, "testItem", "description", user, user, "PENDING");
    }

    private Reservation createMockReservation(Item item, User user, LocalDateTime startAt, LocalDateTime endAt) {
        return new Reservation(item, user, ReservationStatus.PENDING, startAt, endAt);
    }
}