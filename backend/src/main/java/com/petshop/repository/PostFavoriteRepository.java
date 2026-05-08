package com.petshop.repository;

import com.petshop.model.PostFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostFavoriteRepository extends JpaRepository<PostFavorite, Long> {
    List<PostFavorite> findByUserNicknameOrderByCreatedAtDesc(String userNickname);

    Optional<PostFavorite> findFirstByUserNicknameAndPostId(String userNickname, Long postId);

    boolean existsByUserNicknameAndPostId(String userNickname, Long postId);
}
