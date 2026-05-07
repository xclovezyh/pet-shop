package com.petshop.repository;

import com.petshop.model.MarketPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketPostRepository extends JpaRepository<MarketPost, Long> {
}

