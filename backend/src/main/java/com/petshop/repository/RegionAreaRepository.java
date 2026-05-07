package com.petshop.repository;

import com.petshop.model.RegionArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegionAreaRepository extends JpaRepository<RegionArea, Long> {
    List<RegionArea> findByLevelOrderBySortOrderAscIdAsc(String level);

    List<RegionArea> findByParentIdOrderBySortOrderAscIdAsc(Long parentId);
}
