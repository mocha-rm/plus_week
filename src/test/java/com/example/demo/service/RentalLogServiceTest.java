package com.example.demo.service;

import com.example.demo.entity.RentalLog;
import com.example.demo.entity.Reservation;
import com.example.demo.repository.RentalLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RentalLogServiceTest {
    @Mock
    RentalLogRepository rentalLogRepository;

    @InjectMocks
    RentalLogService rentalLogService;


    @Test
    @DisplayName("RentalLog가 null이 아니라면 저장")
    void save() {
        // Given
        Reservation mockReservation = new Reservation();
        RentalLog rentalLog = new RentalLog(mockReservation, "logMessage", "SUCCESS");

        // When
        when(rentalLogRepository.save(any(RentalLog.class))).thenReturn(rentalLog);
        rentalLogService.save(rentalLog);
        when(rentalLogRepository.findById(rentalLog.getId())).thenReturn(Optional.of(rentalLog));
        RentalLog findRentalLog = rentalLogRepository.findById(rentalLog.getId()).get();

        // Then
        assertThat(findRentalLog.getReservation()).isEqualTo(rentalLog.getReservation());
        assertThat(findRentalLog.getLogMessage()).isEqualTo(rentalLog.getLogMessage());
        assertThat(findRentalLog.getLogType()).isEqualTo(rentalLog.getLogType());
    }

    @Test
    @DisplayName("RentalLog가 null이라면 예외 출력")
    void saveException() {
        // Given
        RentalLog rentalLog = null;

        // When
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> rentalLogService.save(rentalLog)
        );

        // Then
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}