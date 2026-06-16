package com.studentmanagement.repository;

import com.studentmanagement.model.Course;
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
public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {

    // ── Derived: find ───────────────────────────────────────────────────────
    Optional<Course> findByCourseCode(String courseCode);

    List<Course> findByCourseNameContainingIgnoreCase(String courseName);

    List<Course> findBySemesterAndAcademicYear(String semester, String academicYear);

    List<Course> findByCreditsGreaterThanEqual(Integer credits);

    List<Course> findByCreditsBetween(Integer minCredits, Integer maxCredits);

    List<Course> findByDepartment_Id(Long departmentId);

    List<Course> findByDepartment_Code(String departmentCode);

    List<Course> findByInstructor_Id(Long instructorId);

    List<Course> findByDepartment_CodeAndSemester(String departmentCode, String semester);

    List<Course> findByAcademicYearOrderByCourseNameAsc(String academicYear);

    // ── Derived: count & exists ─────────────────────────────────────────────
    boolean existsByCourseCode(String courseCode);

    long countByDepartment_Id(Long departmentId);

    long countByInstructor_Id(Long instructorId);

    // ── Derived: pagination ───────────────────────────────────────────────────
    Page<Course> findByDepartment_Code(String departmentCode, Pageable pageable);

    Page<Course> findBySemesterAndAcademicYear(String semester, String academicYear, Pageable pageable);

    // ── JPQL ──────────────────────────────────────────────────────────────────
    @Query("SELECT c FROM Course c WHERE c.department.code = :deptCode AND c.credits >= :minCredits")
    List<Course> findByDepartmentCodeAndMinCredits(
            @Param("deptCode") String deptCode,
            @Param("minCredits") Integer minCredits);

    @Query("SELECT c FROM Course c JOIN c.instructor i WHERE i.employeeId = :employeeId")
    List<Course> findByInstructorEmployeeId(@Param("employeeId") String employeeId);

    @Query("SELECT c FROM Course c WHERE c.semester = :semester AND c.academicYear = :year ORDER BY c.credits DESC")
    List<Course> findByTermOrderByCreditsDesc(
            @Param("semester") String semester,
            @Param("year") String academicYear);

    @Query("SELECT SUM(c.credits) FROM Course c WHERE c.department.id = :departmentId")
    Long sumCreditsByDepartmentId(@Param("departmentId") Long departmentId);

    @Query("SELECT c FROM Course c WHERE SIZE(c.enrollments) = 0")
    List<Course> findCoursesWithNoEnrollments();

    // ── Native SQL ────────────────────────────────────────────────────────────
    @Query(value = "SELECT * FROM courses WHERE course_code = :code", nativeQuery = true)
    Optional<Course> findByCourseCodeNative(@Param("code") String courseCode);

    @Query(value = """
            SELECT c.* FROM courses c
            INNER JOIN departments d ON c.department_id = d.id
            WHERE d.code = :deptCode AND c.semester = :semester
            """, nativeQuery = true)
    List<Course> findByDepartmentAndSemesterNative(
            @Param("deptCode") String deptCode,
            @Param("semester") String semester);

    @Query(value = """
            SELECT c.* FROM courses c
            WHERE c.academic_year = :year
            ORDER BY c.credits DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Course> findTopByCreditsForYearNative(
            @Param("year") String academicYear,
            @Param("limit") int limit);

    // ── Modifying ─────────────────────────────────────────────────────────────
    @Modifying
    @Transactional
    @Query("UPDATE Course c SET c.instructor.id = :instructorId WHERE c.id = :courseId")
    int assignInstructor(@Param("courseId") Long courseId, @Param("instructorId") Long instructorId);

    @Modifying
    @Transactional
    @Query("UPDATE Course c SET c.description = :description WHERE c.courseCode = :courseCode")
    int updateDescriptionByCourseCode(
            @Param("courseCode") String courseCode,
            @Param("description") String description);
}
