package com.example.backend.HotelOwner.service;

import com.example.backend.HotelOwner.domain.*;
import com.example.backend.HotelOwner.dto.OwnerRoomDto;
import com.example.backend.HotelOwner.dto.OwnerRoomDto.UpdateRequest;
import com.example.backend.HotelOwner.repository.*;
import com.example.backend.authlogin.domain.User;
import com.example.backend.authlogin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
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
        
        Hotel hotel = hotelRepository.findByOwner(owner)
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

        Hotel hotel = hotelRepository.findByOwner(owner)
                .orElseThrow(() -> new IllegalArgumentException("소유주에게 할당된 호텔이 없습니다."));
        
        List<Room> rooms = roomRepository.findAllByHotel(hotel);

        return rooms.stream()
                .map(OwnerRoomDto.ListResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OwnerRoomDto.DetailResponse getRoomDetails(Long ownerId, Long roomId) throws AccessDeniedException {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("객실을 찾을 수 없습니다. ID: " + roomId));
        
        if (!room.getHotel().getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("이 객실을 조회할 권한이 없습니다.");
        }
        
        return OwnerRoomDto.DetailResponse.fromEntity(room);
    }

    //  객실 수정 로직
    public void updateRoom(Long ownerId, Long roomId, UpdateRequest request) throws AccessDeniedException {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 객실을 찾을 수 없습니다. ID: " + roomId));
        
        // 소유권 검증: 이 객실이 현재 로그인한 업주의 호텔에 속해 있는지 확인
        if (!room.getHotel().getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("이 객실을 수정할 권한이 없습니다.");
        }

        // DTO의 데이터로 Room 엔티티의 필드를 업데이트
        room.setName(request.getName());
        room.setRoomType(Room.RoomType.valueOf(request.getRoomType()));
        room.setPrice(request.getPrice());
        room.setRoomSize(request.getSize() != null ? request.getSize() + "m²" : null);
        room.setRoomCount(request.getRoomCount());
        room.setCapacityMin(request.getCapacityMin());
        room.setCapacityMax(request.getCapacityMax());
        room.setCheckInTime(request.getCheckInTime());
        room.setCheckOutTime(request.getCheckOutTime());
        room.setSmoke(request.getFacilities().isSmoke());
        room.setBath(request.getFacilities().getBath());
        room.setAircon(request.getFacilities().isAircon());
        room.setWifi(request.getFacilities().isWifi());
        room.setFreeWater(request.getFacilities().isFreeWater());
        room.setHasWindow(request.getFacilities().isHasWindow());

        // 이미지는 별도의 API로 처리하거나, 여기서 기존 이미지를 삭제하고 새로 추가하는 로직을 구현할 수 있습니다.
        // 여기서는 텍스트 정보만 수정하는 것으로 가정합니다.

        roomRepository.save(room);
    }

    // 객실 삭제 로직
    public void deleteRoom(Long ownerId, Long roomId) throws AccessDeniedException {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 객실을 찾을 수 없습니다. ID: " + roomId));

        // 소유권 검증
        if (!room.getHotel().getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("이 객실을 삭제할 권한이 없습니다.");
        }

        // TODO: 이미지 파일 시스템에서 실제 파일 삭제 로직 (필요 시 구현)
        
        roomRepository.delete(room);
    }
}