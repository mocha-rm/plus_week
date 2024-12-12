package com.example.demo.repository;

import com.example.demo.dto.ReservationResponseDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.demo.entity.QItem.item;
import static com.example.demo.entity.QReservation.reservation;
import static com.example.demo.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class ReservationCustomRepositoryImpl implements ReservationCustomRepository {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<ReservationResponseDto> searchReservations(Long userId, Long itemId) {
        BooleanBuilder predicate = new BooleanBuilder();

        if (userId != null) {
            predicate.and(reservation.user.id.eq(userId));
        }
        if (itemId != null) {
            predicate.and(reservation.item.id.eq(itemId));
        }


        return jpaQueryFactory
                .select(Projections.constructor(
                        ReservationResponseDto.class,
                        reservation.id,
                        reservation.user.nickname,
                        reservation.item.name,
                        reservation.startAt,
                        reservation.endAt
                ))
                .from(reservation)
                .join(reservation.user, user)
                .join(reservation.item, item)
                .where(predicate)
                .fetch();
    }
}
