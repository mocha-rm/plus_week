package com.example.demo.repository;

import com.example.demo.dto.ReservationResponseDto;

import java.util.List;

public interface ReservationCustomRepository {
    List<ReservationResponseDto> searchReservations(Long userId, Long itemId);
}
