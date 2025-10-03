package com.example.backend.HotelOwner.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalTime;

import com.example.backend.HotelOwner.domain.Room;

public class OwnerRoomDto {

    @Getter
    @Setter
    public static class RegisterRequest {
        private String name;
        private String roomType;
        private Integer price;
        private Integer size; // 프론트에서는 숫자, 서비스에서 문자열로 변환
        private Integer roomCount;
        private Integer capacityMin;
        private Integer capacityMax;
        private LocalTime checkInTime;
        private LocalTime checkOutTime;
        private Facilities facilities;

        @Getter
        @Setter
        public static class Facilities {
            private boolean smoke;
            private int bath; // 0 또는 1
            private boolean aircon;
            private boolean wifi;
            private boolean freeWater;
            private boolean hasWindow;
        }
    }

    // 객실 목록 조회를 위한 응답 DTO
    @Getter
    @Builder
    public static class ListResponse {
        private Long id;
        private String name;
        private String roomType;
        private String price; // 포맷팅된 가격 (예: "150,000원")
        private String capacity; // 예: "2 / 4" 

        // Entity를 DTO로 변환하는 정적 메소드
        public static ListResponse fromEntity(Room room) {
            return ListResponse.builder()
                    .id(room.getId())
                    .name(room.getName())
                    .roomType(room.getRoomType() != null ? room.getRoomType().name() : "타입 없음")
                    .price(String.format("%,d원", room.getPrice()))
                    .capacity(room.getCapacityMin() + " / " + room.getCapacityMax())
                    .build();
        }
    }
}