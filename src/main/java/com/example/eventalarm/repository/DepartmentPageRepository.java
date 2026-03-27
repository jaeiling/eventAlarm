package com.example.eventalarm.repository;

import com.example.eventalarm.domain.DepartmentPage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentPageRepository extends JpaRepository<DepartmentPage, Long> {
    boolean existsBySerialNumber(String serialNumber);
    Optional<DepartmentPage> findBySerialNumber(String serialNumber);
    java.util.List<DepartmentPage> findAllByOrderByCreatedAtDesc();
    Optional<DepartmentPage> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsByUniversityNameAndDepartmentName(String universityName, String departmentName);
    boolean existsByUniversityNameAndDepartmentNameAndIdNot(String universityName, String departmentName, Long id);
}
