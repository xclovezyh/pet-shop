package com.petshop.repository;

import com.petshop.model.RegionArea;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RegionAreaRepository extends JpaRepository<RegionArea, Long> {
    List<RegionArea> findByLevelOrderBySortOrderAscIdAsc(String level);

    List<RegionArea> findByParentIdOrderBySortOrderAscIdAsc(Long parentId);

    @Query(value = "select region from RegionArea region " +
            "order by case " +
            "when region.level = 'province' then 1 " +
            "when region.level = 'city' then 2 " +
            "when region.level = 'district' then 3 " +
            "else 4 end, " +
            "coalesce(region.sortOrder, 0), coalesce(region.areaCode, ''), region.id",
            countQuery = "select count(region) from RegionArea region")
    Page<RegionArea> findAdminPage(Pageable pageable);
}
