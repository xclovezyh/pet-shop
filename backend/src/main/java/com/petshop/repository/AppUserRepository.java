package com.petshop.repository;

import com.petshop.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByNickname(String nickname);
    Optional<AppUser> findByUsername(String username);
    Optional<AppUser> findByPhone(String phone);
    boolean existsByUsername(String username);
    boolean existsByPhone(String phone);
}

