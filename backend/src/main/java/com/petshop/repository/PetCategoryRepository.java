package com.petshop.repository;

import com.petshop.model.PetCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetCategoryRepository extends JpaRepository<PetCategory, Long> {
}

