package com.petshop.repository;

import com.petshop.model.ReferenceOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReferenceOptionRepository extends JpaRepository<ReferenceOption, Long> {
    List<ReferenceOption> findByOptionTypeOrderBySortOrderAscIdAsc(String optionType);
}
