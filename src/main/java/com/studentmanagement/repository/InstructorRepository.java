package com.studentmanagement.repository;

import com.studentmanagement.model.Instructor;
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
public interface InstructorRepository extends JpaRepository<Instructor, Long>, JpaSpecificationExecutor<Instructor> {

    // ── Derived: find ───────────────────────────────────────────────────────
    Optional<Instructor> findByEmployeeId(String employeeId);

    Optional<Instructor> findByEmail(String email);

    List<Instructor> findByFirstNameAndLastName(String firstName, String lastName);

    List<Instructor> findByLastNameContainingIgnoreCase(String lastName);

    List<Instructor> findBySpecializationContainingIgnoreCase(String specialization);

    List<Instructor> findByDepartment_Id(Long departmentId);

    List<Instructor> findByDepartment_Code(String departmentCode);

    List<Instructor> findByDepartment_IdOrderByLastNameAsc(Long departmentId);

    // ── Derived: count & exists ─────────────────────────────────────────────
    boolean existsByEmail(String email);

    boolean existsByEmployeeId(String employeeId);

    long countByDepartment_Id(Long departmentId);

    // ── Derived: pagination ───────────────────────────────────────────────────
    Page<Instructor> findByDepartment_Code(String departmentCode, Pageable pageable);

    // ── JPQL ──────────────────────────────────────────────────────────────────
    @Query("SELECT i FROM Instructor i WHERE i.department.code = :deptCode AND i.specialization IS NOT NULL")
    List<Instructor> findByDepartmentCodeWithSpecialization(@Param("deptCode") String deptCode);

    @Query("SELECT i FROM Instructor i JOIN i.courses c WHERE c.courseCode = :courseCode")
    Optional<Instructor> findByCourseCode(@Param("courseCode") String courseCode);

    @Query("SELECT i FROM Instructor i WHERE LOWER(i.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "OR LOWER(i.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Instructor> searchByName(@Param("name") String name);

    @Query("SELECT COUNT(c) FROM Instructor i JOIN i.courses c WHERE i.id = :instructorId")
    long countCoursesByInstructorId(@Param("instructorId") Long instructorId);

    // ── Native SQL ────────────────────────────────────────────────────────────
    @Query(value = """
            SELECT i.* FROM instructors i
            INNER JOIN departments d ON i.department_id = d.id
            WHERE d.code = :deptCode
            """, nativeQuery = true)
    List<Instructor> findByDepartmentCodeNative(@Param("deptCode") String deptCode);

    @Query(value = "SELECT * FROM instructors WHERE email LIKE CONCAT('%', :domain)", nativeQuery = true)
    List<Instructor> findByEmailDomainNative(@Param("domain") String domain);

    // ── Modifying ─────────────────────────────────────────────────────────────
    @Modifying
    @Transactional
    @Query("UPDATE Instructor i SET i.phone = :phone WHERE i.id = :id")
    int updatePhoneById(@Param("id") Long id, @Param("phone") String phone);

    @Modifying
    @Transactional
    @Query("UPDATE Instructor i SET i.specialization = :specialization WHERE i.employeeId = :employeeId")
    int updateSpecializationByEmployeeId(
            @Param("employeeId") String employeeId,
            @Param("specialization") String specialization);
}
