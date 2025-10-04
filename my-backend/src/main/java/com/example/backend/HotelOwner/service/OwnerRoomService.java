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
                .roomType(Room.RoomType.valueOf(request.getRoomType()))
                .price(request.getPrice())
                .roomSize(request.getSize() != null ? request.getSize() + "m²" : null)
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
                        .sortNo(i)
                        .isCover(i == 0)
                        .build();
                roomImageRepository.save(roomImage);
            });
        }
        
        return savedRoom.getId();
    }

    @Transactional(readOnly = true)
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

    @Transactional
    public void updateRoom(Long ownerId, Long roomId, UpdateRequest request, List<MultipartFile> newImages) throws AccessDeniedException {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 객실을 찾을 수 없습니다. ID: " + roomId));
        
        if (!room.getHotel().getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("이 객실을 수정할 권한이 없습니다.");
        }

        // 1. 텍스트 정보 업데이트
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

        // 2. 삭제 요청된 이미지 처리
        if (request.getDeletedImages() != null && !request.getDeletedImages().isEmpty()) {
            List<RoomImage> imagesToDelete = room.getImages().stream()
                .filter(img -> request.getDeletedImages().contains(img.getUrl()))
                .collect(Collectors.toList());
            
            // DB에서 이미지 정보 삭제
            room.getImages().removeAll(imagesToDelete);
            roomImageRepository.deleteAll(imagesToDelete);

            // TODO: 실제 서버에서 파일 삭제 로직 추가 (필요 시)
            // imagesToDelete.forEach(img -> fileStorageService.deleteFile(img.getUrl()));
        }

        // 3. 새로 추가된 이미지 저장
        if (newImages != null && !newImages.isEmpty()) {
            int maxSortNo = room.getImages().isEmpty() ? -1 : 
                           room.getImages().stream()
                                .mapToInt(RoomImage::getSortNo)
                                .max()
                                .orElse(-1);

            // 현재 커버 이미지가 있는지 확인
            boolean hasCover = room.getImages().stream()
                                   .anyMatch(RoomImage::isCover);

            for (int i = 0; i < newImages.size(); i++) {
                MultipartFile imageFile = newImages.get(i);
                String imageUrl = fileStorageService.storeFile(imageFile);

                RoomImage roomImage = RoomImage.builder()
                        .room(room)
                        .url(imageUrl)
                        .sortNo(maxSortNo + 1 + i)
                        .isCover(!hasCover && i == 0) // 커버가 없으면 첫 번째 새 이미지를 커버로
                        .build();
                
                roomImageRepository.save(roomImage);
                room.getImages().add(roomImage);
            }
        }
        
        // 4. 커버 이미지 재설정 (모든 이미지가 삭제된 경우)
        if (!room.getImages().isEmpty()) {
            boolean hasCover = room.getImages().stream()
                                   .anyMatch(RoomImage::isCover);
            if (!hasCover) {
                RoomImage firstImage = room.getImages().stream()
                    .min((a, b) -> Integer.compare(a.getSortNo(), b.getSortNo()))
                    .orElse(null);
                if (firstImage != null) {
                    firstImage.setCover(true);
                }
            }
        }
        
        roomRepository.save(room);
    }

    @Transactional
    public void deleteRoom(Long ownerId, Long roomId) throws AccessDeniedException {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 객실을 찾을 수 없습니다. ID: " + roomId));

        // 소유권 검증
        if (!room.getHotel().getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("이 객실을 삭제할 권한이 없습니다.");
        }

        // TODO: 이미지 파일 시스템에서 실제 파일 삭제 로직 (필요 시 구현)
        // room.getImages().forEach(img -> fileStorageService.deleteFile(img.getUrl()));
        
        roomRepository.delete(room);
    }
}