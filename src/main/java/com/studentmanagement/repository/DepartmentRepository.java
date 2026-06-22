package com.studentmanagement.repository;

import com.studentmanagement.model.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {

    // ── Derived: find ───────────────────────────────────────────────────────
    Optional<Department> findByCode(String code);

    Optional<Department> findByName(String name);

    List<Department> findByNameContainingIgnoreCase(String namePart);

    List<Department> findByHeadOfDepartment(String headOfDepartment);

    List<Department> findByCodeIn(List<String> codes);

    List<Department> findAllByOrderByNameAsc();

    // ── Derived: count & exists ─────────────────────────────────────────────
    boolean existsByCode(String code);

    boolean existsByNameIgnoreCase(String name);

    long countByHeadOfDepartmentIsNotNull();

    // ── Derived: pagination ─────────────────────────────────────────────────
    Page<Department> findByNameContaining(String namePart, Pageable pageable);

    // ── JPQL ──────────────────────────────────────────────────────────────────
        @Query("SELECT d FROM Department d WHERE d.description IS NOT NULL AND d.description <> ''")
    List<Department> findAllWithDescription();

    @Query("SELECT d FROM Department d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(d.code) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Department> searchByNameOrCode(@Param("keyword") String keyword);

    @Query("SELECT d FROM Department d WHERE d.headOfDepartment IS NULL")
    List<Department> findDepartmentsWithoutHead();

    // ── Native SQL ────────────────────────────────────────────────────────────
    @Query(value = "SELECT * FROM departments WHERE code = :code", nativeQuery = true)
    Optional<Department> findByCodeNative(@Param("code") String code);

    @Query(value = "SELECT COUNT(*) FROM departments WHERE head_of_department IS NOT NULL", nativeQuery = true)
    long countDepartmentsWithHeadNative();

    // ── Modifying ─────────────────────────────────────────────────────────────
    @Modifying
    @Transactional
    @Query("UPDATE Department d SET d.headOfDepartment = :head WHERE d.id = :id")
    int updateHeadOfDepartment(@Param("id") Long id, @Param("head") String headOfDepartment);

    @Modifying
    @Transactional
    @Query("UPDATE Department d SET d.description = :description WHERE d.code = :code")
    int updateDescriptionByCode(@Param("code") String code, @Param("description") String description);
}
