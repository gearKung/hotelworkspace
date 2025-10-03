package com.example.backend.HotelOwner.service;

import com.example.backend.HotelOwner.domain.*;
import com.example.backend.HotelOwner.dto.OwnerRoomDto;
import com.example.backend.HotelOwner.repository.*;
import com.example.backend.authlogin.domain.User;
import com.example.backend.authlogin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class OwnerRoomService {

    private final OwnerRoomRepository roomRepository;
    private final RoomImageRepository roomImageRepository;
    private final OwnerHotelRepository hotelRepository; 
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public Long registerRoom(Long ownerId, OwnerRoomDto.RegisterRequest request, List<MultipartFile> images) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("소유주 정보를 찾을 수 없습니다."));
        
        Hotel hotel = hotelRepository.findByUser(owner)
                .orElseThrow(() -> new IllegalArgumentException("소유주에게 할당된 호텔이 없습니다."));

        Room room = Room.builder()
                .hotel(hotel)
                .name(request.getName())
                .roomType(Room.RoomType.valueOf(request.getRoomType())) // String to Enum
                .price(request.getPrice())
                .roomSize(request.getSize() != null ? request.getSize() + "m²" : null) // 숫자 + 단위
                .roomCount(request.getRoomCount())
                .capacityMin(request.getCapacityMin())
                .capacityMax(request.getCapacityMax())
                .checkInTime(request.getCheckInTime())
                .checkOutTime(request.getCheckOutTime())
                .smoke(request.getFacilities().isSmoke())
                .bath(request.getFacilities().getBath())
                .aircon(request.getFacilities().isAircon())
                .wifi(request.getFacilities().isWifi())
                .freeWater(request.getFacilities().isFreeWater())
                .hasWindow(request.getFacilities().isHasWindow())
                .build();
        
        Room savedRoom = roomRepository.save(room);

        if (images != null && !images.isEmpty()) {
            IntStream.range(0, images.size()).forEach(i -> {
                String imageUrl = fileStorageService.storeFile(images.get(i));
                RoomImage roomImage = RoomImage.builder()
                        .room(savedRoom)
                        .url(imageUrl)
                        .sortNo(i) // 프론트 순서 반영
                        .isCover(i == 0) // 첫 이미지를 커버로 지정
                        .build();
                roomImageRepository.save(roomImage);
            });
        }
        
        return savedRoom.getId();
    }

    // 특정 호텔의 객실 목록 조회
    public List<OwnerRoomDto.ListResponse> getRoomsForOwner(Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("소유주 정보를 찾을 수 없습니다. ID: " + ownerId));

        Hotel hotel = hotelRepository.findByUser(owner)
                .orElseThrow(() -> new IllegalArgumentException("소유주에게 할당된 호텔이 없습니다."));
        
        List<Room> rooms = roomRepository.findAllByHotel(hotel);

        return rooms.stream()
                .map(OwnerRoomDto.ListResponse::fromEntity)
                .collect(Collectors.toList());
    }
}