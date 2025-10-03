package com.example.backend.HotelOwner.controller;

import com.example.backend.HotelOwner.dto.OwnerRoomDto;
import com.example.backend.HotelOwner.service.OwnerRoomService;
import com.example.backend.authlogin.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/owner/rooms")
@RequiredArgsConstructor
public class OwnerRoomController {

    private final OwnerRoomService ownerRoomService;
    private final JwtUtil jwtUtil;

    // 객실 등록 API
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Long> registerRoom(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestPart("roomRequest") OwnerRoomDto.RegisterRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        String token = authorizationHeader.substring(7);
        Long ownerId = jwtUtil.extractUserId(token);

        Long roomId = ownerRoomService.registerRoom(ownerId, request, images);
        
        return ResponseEntity.ok(roomId);
    }
    
    // 객실 목록 조회 API
    @GetMapping
    public ResponseEntity<List<OwnerRoomDto.ListResponse>> getMyRooms(
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = authorizationHeader.substring(7);
        Long ownerId = jwtUtil.extractUserId(token);
        
        List<OwnerRoomDto.ListResponse> rooms = ownerRoomService.getRoomsForOwner(ownerId);
        
        return ResponseEntity.ok(rooms);
    }
}