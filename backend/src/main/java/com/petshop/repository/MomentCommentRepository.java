package com.petshop.repository;

import com.petshop.model.MomentComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MomentCommentRepository extends JpaRepository<MomentComment, Long> {
    List<MomentComment> findByMomentIdOrderByCreatedAtAscIdAsc(Long momentId);
}
