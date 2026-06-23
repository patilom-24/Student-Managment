package com.studentmanagement.service;

import com.studentmanagement.exception.BusinessValidationException;
import com.studentmanagement.exception.ServiceException;
import com.studentmanagement.model.Course;
import com.studentmanagement.model.Department;
import com.studentmanagement.model.Enrollment;
import com.studentmanagement.model.Student;
import com.studentmanagement.model.enums.EnrollmentStatus;
import com.studentmanagement.model.enums.StudentStatus;
import com.studentmanagement.repository.AttendanceRepository;
import com.studentmanagement.repository.CourseRepository;
import com.studentmanagement.repository.DepartmentRepository;
import com.studentmanagement.repository.EnrollmentRepository;
import com.studentmanagement.repository.FeeRepository;
import com.studentmanagement.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Demonstrates @Transactional COMMIT and ROLLBACK behaviour.
 * <p>
 * Rules:
 * - @Transactional starts a DB transaction before the method runs
 * - On success → COMMIT (all saves are permanent)
 * - On RuntimeException / ServiceException → ROLLBACK (all changes in this method are undone)
 * - readOnly = true → no writes, better performance for SELECT queries
 */
@Service
@Transactional(readOnly = true)
public class TransactionService {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final FeeRepository feeRepository;
    private final AttendanceRepository attendanceRepository;

    public TransactionService(
            StudentRepository studentRepository,
            EnrollmentRepository enrollmentRepository,
            DepartmentRepository departmentRepository,
            CourseRepository courseRepository,
            FeeRepository feeRepository,
            AttendanceRepository attendanceRepository) {
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.departmentRepository = departmentRepository;
        this.courseRepository = courseRepository;
        this.feeRepository = feeRepository;
        this.attendanceRepository = attendanceRepository;
    }

    /**
     * COMMIT demo: saves student AND enrollment in one transaction.
     * If enrollment save fails, student save is also rolled back.
     */
    @Transactional(rollbackFor = ServiceException.class)
    public Student commitStudentWithEnrollment(
            String studentId,
            String email,
            String phone,
            String departmentCode,
            String courseCode,
            String semester,
            String academicYear) {

        Department department = departmentRepository.findByCode(departmentCode)
                .orElseThrow(() -> new BusinessValidationException("Department not found: " + departmentCode));
        Course course = courseRepository.findByCourseCode(courseCode)
                .orElseThrow(() -> new BusinessValidationException("Course not found: " + courseCode));

        Student student = Student.builder()
                .studentId(studentId)
                .firstName("Txn")
                .lastName("Commit")
                .email(email)
                .phone(phone)
                .status(StudentStatus.ACTIVE)
                .department(department)
                .build();

        Student saved = studentRepository.save(student);

        Enrollment enrollment = Enrollment.builder()
                .student(saved)
                .course(course)
                .semester(semester)
                .academicYear(academicYear)
                .status(EnrollmentStatus.ENROLLED)
                .build();

        enrollmentRepository.save(enrollment);

        return saved;
    }

    /**
     * ROLLBACK demo: saves student, then throws exception.
     * Student must NOT exist in DB after this method — entire transaction rolls back.
     */
    @Transactional(rollbackFor = ServiceException.class)
    public void rollbackAfterStudentSave(String studentId, String email, String phone, String departmentCode) {
        Department department = departmentRepository.findByCode(departmentCode)
                .orElseThrow(() -> new BusinessValidationException("Department not found: " + departmentCode));

        Student student = Student.builder()
                .studentId(studentId)
                .firstName("Txn")
                .lastName("Rollback")
                .email(email)
                .phone(phone)
                .status(StudentStatus.ACTIVE)
                .department(department)
                .build();

        studentRepository.save(student);

        throw new BusinessValidationException(
                "Simulated failure — transaction will ROLLBACK, student will NOT be saved");
    }

    /**
     * ROLLBACK demo: deletes enrollments then throws before deleting student.
     * Enrollments must still exist after rollback.
     */
    @Transactional(rollbackFor = ServiceException.class)
    public void rollbackDuringDelete(Long studentId) {
        enrollmentRepository.deleteAll(enrollmentRepository.findByStudent_Id(studentId));
        throw new BusinessValidationException(
                "Simulated failure during delete — enrollment delete is ROLLED BACK");
    }

    /**
     * COMMIT demo: deletes all related records + student in one atomic transaction.
     */
    @Transactional(rollbackFor = ServiceException.class)
    public void deleteStudentWithAllRelatedData(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessValidationException("Student not found: " + studentId));

        attendanceRepository.deleteAll(attendanceRepository.findByStudent_Id(studentId));
        feeRepository.deleteAll(feeRepository.findByStudent_Id(studentId));
        enrollmentRepository.deleteAll(enrollmentRepository.findByStudent_Id(studentId));
        studentRepository.delete(student);
    }

    public boolean studentExistsByEmail(String email) {
        return studentRepository.existsByEmail(email);
    }

    public boolean studentExistsByStudentId(String studentId) {
        return studentRepository.existsByStudentId(studentId);
    }

    public long countEnrollmentsByStudentId(Long studentId) {
        return enrollmentRepository.countByStudent_Id(studentId);
    }
}
