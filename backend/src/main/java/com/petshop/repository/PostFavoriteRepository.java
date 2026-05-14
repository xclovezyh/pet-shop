package com.petshop.repository;

import com.petshop.model.PostFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostFavoriteRepository extends JpaRepository<PostFavorite, Long> {
    List<PostFavorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<PostFavorite> findFirstByUserIdAndPostId(Long userId, Long postId);

    boolean existsByUserIdAndPostId(Long userId, Long postId);
}
