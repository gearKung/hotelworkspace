package com.example.backend.HotelOwner.repository;

import com.example.backend.HotelOwner.domain.Hotel;
import com.example.backend.authlogin.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OwnerHotelRepository extends JpaRepository<Hotel, Long> {

    // Spring Data JPA가 메소드 이름을 분석하여 쿼리를 자동으로 생성합니다.
    Optional<Hotel> findByUser(User user);
}