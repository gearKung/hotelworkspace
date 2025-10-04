package com.example.backend.HotelOwner.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import com.example.backend.HotelOwner.domain.Room;
import com.example.backend.HotelOwner.domain.RoomImage;
import com.example.backend.HotelOwner.dto.OwnerRoomDto.RegisterRequest.Facilities;

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

    @Getter
    @Setter
    public static class UpdateRequest extends RegisterRequest {
        // RegisterRequest의 모든 필드를 상속받아 그대로 사용합니다.
    }

    // 객실 목록 조회를 위한 응답 DTO
    @Getter
    @Builder
    public static class ListResponse {
        private Long id;
        private String name;
        private String roomType;
        private String price;
        private String capacity;
        private Integer roomCount;

        public static ListResponse fromEntity(Room room) {
            return ListResponse.builder()
                    .id(room.getId())
                    .name(room.getName())
                    .roomType(room.getRoomType() != null ? room.getRoomType().name() : "타입 없음")
                    .price(String.format("%,d원", room.getPrice()))
                    .capacity(room.getCapacityMin() + " / " + room.getCapacityMax())
                    .roomCount(room.getRoomCount())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class DetailResponse {
        private String name;
        private String roomType;
        private Integer price;
        private Integer size;
        private Integer roomCount;
        private Integer capacityMin;
        private Integer capacityMax;
        private LocalTime checkInTime;
        private LocalTime checkOutTime;
        private Facilities facilities;
        private List<String> imageUrls;

        public static DetailResponse fromEntity(Room room) {
            Facilities facilitiesDto = new RegisterRequest.Facilities();
            facilitiesDto.setSmoke(room.getSmoke());
            facilitiesDto.setBath(room.getBath());
            facilitiesDto.setAircon(room.getAircon());
            facilitiesDto.setWifi(room.getWifi());
            facilitiesDto.setFreeWater(room.getFreeWater());
            facilitiesDto.setHasWindow(room.getHasWindow());

            List<String> images = room.getImages().stream()
                .map(RoomImage::getUrl)
                .collect(Collectors.toList());

            return DetailResponse.builder()
                    .name(room.getName())
                    .roomType(room.getRoomType().name())
                    .price(room.getPrice())
                    .size(room.getRoomSize() != null ? Integer.parseInt(room.getRoomSize().replaceAll("[^0-9]", "")) : null)
                    .roomCount(room.getRoomCount())
                    .capacityMin(room.getCapacityMin())
                    .capacityMax(room.getCapacityMax())
                    .checkInTime(room.getCheckInTime())
                    .checkOutTime(room.getCheckOutTime())
                    .facilities(facilitiesDto)
                    .imageUrls(images)
                    .build();
        }
    }
}