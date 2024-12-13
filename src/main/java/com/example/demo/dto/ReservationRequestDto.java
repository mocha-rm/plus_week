package com.example.demo.dto;

import com.example.demo.status.ReservationStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReservationRequestDto {
    private Long itemId;
    private Long userId;
    private ReservationStatus status;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
}
