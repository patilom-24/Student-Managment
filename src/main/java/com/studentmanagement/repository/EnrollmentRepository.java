package com.studentmanagement.repository;

import com.studentmanagement.model.Enrollment;
import com.studentmanagement.model.enums.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long>, JpaSpecificationExecutor<Enrollment> {

    // ── Derived: find ───────────────────────────────────────────────────────
    List<Enrollment> findByStudent_Id(Long studentId);

    List<Enrollment> findByCourse_Id(Long courseId);

    List<Enrollment> findByStudent_IdAndStatus(Long studentId, EnrollmentStatus status);

    List<Enrollment> findByCourse_IdAndStatus(Long courseId, EnrollmentStatus status);

    List<Enrollment> findBySemesterAndAcademicYear(String semester, String academicYear);

    List<Enrollment> findByStatus(EnrollmentStatus status);

    List<Enrollment> findByGradeGreaterThanEqual(BigDecimal minGrade);

    List<Enrollment> findByEnrollmentDateBetween(LocalDate start, LocalDate end);

    Optional<Enrollment> findByStudent_IdAndCourse_IdAndSemesterAndAcademicYear(
            Long studentId,
            Long courseId,
            String semester,
            String academicYear);

    List<Enrollment> findByStudent_StudentIdAndAcademicYear(String studentId, String academicYear);

    List<Enrollment> findByCourse_CourseCode(String courseCode);

    // ── Derived: count & exists ─────────────────────────────────────────────
    boolean existsByStudent_IdAndCourse_IdAndSemesterAndAcademicYear(
            Long studentId,
            Long courseId,
            String semester,
            String academicYear);

    long countByCourse_IdAndStatus(Long courseId, EnrollmentStatus status);

    long countByStudent_Id(Long studentId);

    // ── Derived: pagination ───────────────────────────────────────────────────
    Page<Enrollment> findByStudent_Id(Long studentId, Pageable pageable);

    Page<Enrollment> findByCourse_IdAndStatus(Long courseId, EnrollmentStatus status, Pageable pageable);

    // ── JPQL ──────────────────────────────────────────────────────────────────
    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.status = :status " +
           "ORDER BY e.enrollmentDate DESC")
    List<Enrollment> findStudentEnrollmentsByStatus(
            @Param("studentId") Long studentId,
            @Param("status") EnrollmentStatus status);

    @Query("SELECT e FROM Enrollment e WHERE e.course.id = :courseId AND e.grade IS NOT NULL " +
           "ORDER BY e.grade DESC")
    List<Enrollment> findGradedEnrollmentsByCourse(@Param("courseId") Long courseId);

    @Query("SELECT AVG(e.grade) FROM Enrollment e WHERE e.course.id = :courseId AND e.grade IS NOT NULL")
    BigDecimal averageGradeByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT e FROM Enrollment e WHERE e.student.department.code = :deptCode " +
           "AND e.academicYear = :year")
    List<Enrollment> findByStudentDepartmentAndYear(
            @Param("deptCode") String deptCode,
            @Param("year") String academicYear);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId AND e.status = :status")
    long countEnrollmentsByCourseAndStatus(
            @Param("courseId") Long courseId,
            @Param("status") EnrollmentStatus status);

    // ── Native SQL ────────────────────────────────────────────────────────────
    @Query(value = """
            SELECT e.* FROM enrollments e
            INNER JOIN students s ON e.student_id = s.id
            WHERE s.student_id = :studentCode AND e.academic_year = :year
            """, nativeQuery = true)
    List<Enrollment> findByStudentCodeAndYearNative(
            @Param("studentCode") String studentCode,
            @Param("year") String academicYear);

    @Query(value = """
            SELECT e.* FROM enrollments e
            INNER JOIN courses c ON e.course_id = c.id
            WHERE c.course_code = :courseCode AND e.status = :status
            """, nativeQuery = true)
    List<Enrollment> findByCourseCodeAndStatusNative(
            @Param("courseCode") String courseCode,
            @Param("status") String status);

    @Query(value = """
            SELECT AVG(e.grade) FROM enrollments e
            WHERE e.course_id = :courseId AND e.grade IS NOT NULL
            """, nativeQuery = true)
    BigDecimal averageGradeByCourseIdNative(@Param("courseId") Long courseId);

    // ── Modifying ─────────────────────────────────────────────────────────────
    @Modifying
    @Transactional
    @Query("UPDATE Enrollment e SET e.status = :status WHERE e.id = :id")
    int updateStatusById(@Param("id") Long id, @Param("status") EnrollmentStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE Enrollment e SET e.grade = :grade, e.letterGrade = :letterGrade WHERE e.id = :id")
    int updateGradeById(
            @Param("id") Long id,
            @Param("grade") BigDecimal grade,
            @Param("letterGrade") String letterGrade);

    @Modifying
    @Transactional
    @Query("UPDATE Enrollment e SET e.status = :newStatus WHERE e.student.id = :studentId AND e.status = :currentStatus")
    int bulkUpdateStatusByStudent(
            @Param("studentId") Long studentId,
            @Param("currentStatus") EnrollmentStatus currentStatus,
            @Param("newStatus") EnrollmentStatus newStatus);

    @Modifying
    @Transactional
    @Query("DELETE FROM Enrollment e WHERE e.course.id = :courseId AND e.status = :status")
    int deleteByCourseIdAndStatus(
            @Param("courseId") Long courseId,
            @Param("status") EnrollmentStatus status);
}
