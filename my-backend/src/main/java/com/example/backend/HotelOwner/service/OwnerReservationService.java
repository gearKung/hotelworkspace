package com.example.backend.HotelOwner.service;

import com.example.backend.HotelOwner.domain.Hotel;
import com.example.backend.HotelOwner.domain.Room;
import com.example.backend.HotelOwner.dto.OwnerReservationDto;
import com.example.backend.HotelOwner.dto.OwnerReservationDto.CalendarEvent;
import com.example.backend.HotelOwner.repository.OwnerHotelRepository;
import com.example.backend.HotelOwner.repository.OwnerReservationRepository;
import com.example.backend.HotelOwner.repository.OwnerRoomRepository;
import com.example.backend.authlogin.domain.User;
import com.example.backend.authlogin.repository.UserRepository;
import com.example.backend.hotel_reservation.domain.Reservation;
import com.example.backend.hotel_reservation.domain.Reservation.ResStatus;
import com.example.backend.hotel_reservation.domain.Reservation.Status;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerReservationService {

    private final OwnerReservationRepository ownerReservationRepository;
    private final UserRepository userRepository;
    private final OwnerHotelRepository ownerHotelRepository;
    private final OwnerRoomRepository ownerRoomRepository;

    @Transactional(readOnly = true)
    public List<CalendarEvent> getReservationsForOwner(Long ownerId) {
        Hotel hotel = findMyHotel(ownerId);
        List<Reservation> reservations = ownerReservationRepository.findAllByHotelId(hotel.getId());

        Map<String, String> roomTypeColorMap = new HashMap<>();
        roomTypeColorMap.put("스위트룸", "#ef4444"); roomTypeColorMap.put("디럭스룸", "#3b82f6");
        roomTypeColorMap.put("스탠다드룸", "#22c55e"); roomTypeColorMap.put("싱글룸", "#f97316");
        roomTypeColorMap.put("트윈룸", "#a855f7");

        return reservations.stream().map(res -> {
            User user = userRepository.findById(res.getUserId()).orElse(null);
            Room room = ownerRoomRepository.findById(res.getRoomId()).orElse(null);

            String guestName = (user != null) ? user.getName() : "알 수 없음";
            String roomTypeName = (room != null) ? room.getRoomType().name() : "기타";
            
            return OwnerReservationDto.CalendarEvent.builder()
                .id(res.getId())
                .title(guestName)
                .start(OwnerReservationDto.toLocalDate(res.getStartDate()))
                .end(OwnerReservationDto.toLocalDate(res.getEndDate()).plusDays(1))
                .color(roomTypeColorMap.getOrDefault(roomTypeName, "#848484"))
                .status(res.getStatus().name())
                .extendedProps(OwnerReservationDto.ExtendedProps.builder()
                    .guestName(guestName)
                    .roomName((room != null) ? room.getName() : "삭제된 객실")
                    .resStatus(res.getResStatus() != null ? res.getResStatus().name() : "RESERVED") // resStatus 추가
                    .build())
                .build();
        }).collect(Collectors.toList());
    }

    // 상세 정보 조회
    @Transactional(readOnly = true)
    public OwnerReservationDto.DetailResponse getReservationDetails(Long ownerId, Long reservationId) throws AccessDeniedException {
        Reservation reservation = findMyReservation(ownerId, reservationId);
        User user = userRepository.findById(reservation.getUserId()).orElseThrow();
        Room room = ownerRoomRepository.findById(reservation.getRoomId()).orElseThrow();
        
        return OwnerReservationDto.DetailResponse.builder()
            .id(reservation.getId())
            .hotelName(room.getHotel().getName())
            .guestName(user.getName())
            .guestPhone(user.getPhone())
            .checkInDate(OwnerReservationDto.toLocalDate(reservation.getStartDate()))
            .checkOutDate(OwnerReservationDto.toLocalDate(reservation.getEndDate()))
            .roomType(room.getRoomType().name())
            .numAdult(reservation.getNumAdult())
            .numKid(reservation.getNumKid())
            .reservationStatus(reservation.getStatus().name())
            .resStatus(reservation.getResStatus() != null ? reservation.getResStatus().name() : "RESERVED")
            .build();
    }
    
    // 체크인
    public void checkIn(Long ownerId, Long reservationId) throws AccessDeniedException {
        Reservation reservation = findMyReservation(ownerId, reservationId);
        if (!OwnerReservationDto.toLocalDate(reservation.getStartDate()).equals(LocalDate.now())) {
            throw new IllegalStateException("체크인 날짜가 아닙니다.");
        }
        reservation.setResStatus(ResStatus.CHECKED_IN);
    }
    
    public void checkOut(Long ownerId, Long reservationId) throws AccessDeniedException {
        Reservation reservation = findMyReservation(ownerId, reservationId);
        if (!OwnerReservationDto.toLocalDate(reservation.getEndDate()).equals(LocalDate.now())) {
            throw new IllegalStateException("체크아웃 날짜가 아닙니다.");
        }
        reservation.setResStatus(ResStatus.CHECKED_OUT);
    }

    public void cancelCheckInOrOut(Long ownerId, Long reservationId) throws AccessDeniedException {
        findMyReservation(ownerId, reservationId).setResStatus(ResStatus.RESERVED);
    }
    
    public void cancelReservation(Long ownerId, Long reservationId) throws AccessDeniedException {
        Reservation reservation = findMyReservation(ownerId, reservationId);
        if (OwnerReservationDto.toLocalDate(reservation.getStartDate()).isBefore(LocalDate.now())) {
            throw new IllegalStateException("이미 지난 예약은 취소할 수 없습니다.");
        }
        reservation.setStatus(Status.CANCELLED);
    }
    
    private Hotel findMyHotel(Long ownerId) {
        User owner = userRepository.findById(ownerId).orElseThrow(() -> new IllegalArgumentException("소유주를 찾을 수 없습니다."));
        return ownerHotelRepository.findByOwner(owner).orElseThrow(() -> new IllegalArgumentException("소유주에게 할당된 호텔이 없습니다."));
    }

    private Reservation findMyReservation(Long ownerId, Long reservationId) throws AccessDeniedException {
        Reservation reservation = ownerReservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
        Room room = ownerRoomRepository.findById(reservation.getRoomId()).orElseThrow();

        if (!room.getHotel().getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }
        return reservation;
    }

    // private CalendarEvent toCalendarEvent(Reservation reservation) {
    //     // 예약자(User) 이름 조회
    //     String guestName = userRepository.findById(reservation.getUserId())
    //             .map(User::getName)
    //             .orElse("알 수 없음");

    //     // 객실(Room) 이름 조회
    //     String roomName = ownerRoomRepository.findById(reservation.getRoomId())
    //             .map(Room::getName)
    //             .orElse("삭제된 객실");

    //     // Instant 타입을 LocalDate 또는 LocalDateTime으로 변환
    //     LocalDate checkInDate = reservation.getStartDate().atZone(ZoneId.systemDefault()).toLocalDate();
    //     LocalDate checkOutDate = reservation.getEndDate().atZone(ZoneId.systemDefault()).toLocalDate();
    //     LocalDateTime createdAtDateTime = reservation.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime();

    //     // 상세 정보 DTO 생성
    //     OwnerReservationDto.ExtendedProps props = OwnerReservationDto.ExtendedProps.builder()
    //         .guestName(guestName)
    //         .roomName(roomName)
    //         .status(reservation.getStatus().name())
    //         .checkIn(checkInDate)
    //         .checkOut(checkOutDate)
    //         .createdAt(createdAtDateTime)
    //         .build();

    //     // 최종 캘린더 이벤트 DTO 생성
    //     return OwnerReservationDto.CalendarEvent.builder()
    //             .id(reservation.getId())
    //             .title(guestName + " - " + roomName)
    //             .start(checkInDate)
    //             .end(checkOutDate.plusDays(1))
    //             .color(OwnerReservationDto.CalendarEvent.getColorByStatus(reservation.getStatus()))
    //             .extendedProps(props)
    //             .build();
    // }
}