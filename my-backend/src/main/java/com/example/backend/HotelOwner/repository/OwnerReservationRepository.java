package com.example.backend.HotelOwner.repository;

import com.example.backend.hotel_reservation.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OwnerReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * 특정 호텔 ID에 속한 모든 객실의 예약 목록을 조회하는 JPQL 쿼리
     * Reservation -> Room -> Hotel을 거쳐서 필터링합니다.
     * @param hotelId 호텔의 ID
     * @return 해당 호텔의 모든 예약 목록
     */
    @Query("SELECT res FROM Reservation res WHERE res.roomId IN " +
           "(SELECT r.id FROM Room r WHERE r.hotel.id = :hotelId)")
    List<Reservation> findAllByHotelId(@Param("hotelId") Long hotelId);
}