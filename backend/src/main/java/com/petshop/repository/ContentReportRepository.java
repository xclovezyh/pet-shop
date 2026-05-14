package com.petshop.repository;

import com.petshop.model.ContentReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentReportRepository extends JpaRepository<ContentReport, Long> {
    List<ContentReport> findByReporterOrderByCreatedAtDesc(String reporter);
    List<ContentReport> findByReporterUserIdOrderByCreatedAtDesc(Long reporterUserId);
    List<ContentReport> findAllByOrderByCreatedAtDesc();
}
