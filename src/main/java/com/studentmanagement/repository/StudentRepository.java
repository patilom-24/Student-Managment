package com.studentmanagement.repository;

import com.studentmanagement.model.Address;
import com.studentmanagement.model.Student;
import com.studentmanagement.model.enums.Gender;
import com.studentmanagement.model.enums.StudentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {

    // ── Derived: find ───────────────────────────────────────────────────────
    Optional<Student> findByStudentId(String studentId);

    Optional<Student> findByEmail(String email);

    Student findStudentByEmail(String email);

    List<Student> findByFirstNameAndLastName(String firstName, String lastName);

    List<Student> findByStatus(StudentStatus status);

    List<Student> findByStatusIn(List<StudentStatus> statuses);

    List<Student> findByStatusAndGender(StudentStatus status, Gender gender);

    List<Student> findByGender(Gender gender);

    List<Student> findByFirstNameContainingIgnoreCase(String firstName);

    List<Student> findByLastNameStartingWithIgnoreCase(String lastNamePrefix);

    List<Student> findByEmailEndingWith(String domain);

    List<Student> findByPhoneIsNotNull();

    List<Student> findByDepartment_Id(Long departmentId);

    List<Student> findByDepartment_Code(String departmentCode);

    List<Student> findByAddress_City(String city);

    List<Student> findByAddress_State(String state);

    List<Student> findByEnrollmentDateAfter(LocalDate date);

    List<Student> findByEnrollmentDateBetween(LocalDate start, LocalDate end);

    List<Student> findByDateOfBirthBefore(LocalDate date);

    List<Student> findByStatusOrderByLastNameAscFirstNameAsc(StudentStatus status);

    List<Student> findByDepartment_CodeOrderByEnrollmentDateDesc(String departmentCode);



    // ── Derived: count & exists ─────────────────────────────────────────────
    long countByStatus(StudentStatus status);

    long countByDepartment_Id(Long departmentId);

    long countByGender(Gender gender);

    boolean existsByEmail(String email);

    boolean existsByStudentId(String studentId);

    boolean existsByPhone(String phone);

    // ── Derived: pagination ───────────────────────────────────────────────────
    Page<Student> findByStatus(StudentStatus status, Pageable pageable);

    Page<Student> findByDepartment_Code(String departmentCode, Pageable pageable);

    Page<Student> findByFirstNameContainingIgnoreCase(String firstName, Pageable pageable);

    // ── JPQL ──────────────────────────────────────────────────────────────────
    @Query("SELECT s FROM Student s WHERE s.enrollmentDate >= :fromDate")
    List<Student> findEnrolledOnOrAfter(@Param("fromDate") LocalDate fromDate);

    @Query("SELECT s FROM Student s JOIN s.address a WHERE a.city = :city")
    List<Student> findByAddressCity(@Param("city") String city);

    @Query("SELECT s FROM Student s WHERE s.status = :status AND s.lastName LIKE CONCAT('%', :lastName, '%')")
    List<Student> findByStatusAndLastNameLike(
            @Param("status") StudentStatus status,
            @Param("lastName") String lastName);

    @Query("SELECT s FROM Student s WHERE LOWER(s.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Student> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT s FROM Student s WHERE s.department.code = :deptCode AND s.status = :status")
    List<Student> findByDepartmentCodeAndStatus(
            @Param("deptCode") String deptCode,
            @Param("status") StudentStatus status);

    @Query("SELECT s FROM Student s WHERE s.department IS NULL")
    List<Student> findStudentsWithoutDepartment();

    @Query("SELECT COUNT(s) FROM Student s WHERE s.address.city = :city")
    long countByAddressCity(@Param("city") String city);

    // ── Native SQL ────────────────────────────────────────────────────────────
    @Query(value = "SELECT * FROM students WHERE status = :status", nativeQuery = true)
    List<Student> findByStatusNative(@Param("status") String status);

    @Query(value = """
            SELECT s.* FROM students s
            INNER JOIN addresses a ON s.address_id = a.id
            WHERE a.state = :state
            """, nativeQuery = true)
    List<Student> findByAddressStateNative(@Param("state") String state);

    @Query(value = """
            SELECT s.* FROM students s
            INNER JOIN departments d ON s.department_id = d.id
            WHERE d.code = :deptCode
            ORDER BY s.last_name ASC
            """, nativeQuery = true)
    List<Student> findByDepartmentCodeNative(@Param("deptCode") String deptCode);

    @Query(value = """
            SELECT COUNT(*) FROM students s
            INNER JOIN enrollments e ON s.id = e.student_id
            WHERE e.status = :enrollmentStatus
            """, nativeQuery = true)
    long countStudentsWithEnrollmentStatusNative(@Param("enrollmentStatus") String enrollmentStatus);

    // ── Modifying ─────────────────────────────────────────────────────────────
    @Modifying
    @Transactional
    @Query("UPDATE Student s SET s.status = :status WHERE s.id = :id")
    int updateStatusById(@Param("id") Long id, @Param("status") StudentStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE Student s SET s.phone = :phone WHERE s.studentId = :studentId")
    int updatePhoneByStudentId(@Param("studentId") String studentId, @Param("phone") String phone);

    @Modifying
    @Transactional
    @Query("UPDATE Student s SET s.department.id = :departmentId WHERE s.id = :studentId")
    int assignDepartment(@Param("studentId") Long studentId, @Param("departmentId") Long departmentId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Student s WHERE s.status = :status")
    int deleteByStatus(@Param("status") StudentStatus status);
}
