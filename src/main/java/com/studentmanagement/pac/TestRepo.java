package com.studentmanagement.pac;

import com.studentmanagement.dto.request.CourseCreateRequest;
import com.studentmanagement.dto.request.StudentRegistrationRequest;
import com.studentmanagement.dto.response.StudentResponse;
import com.studentmanagement.exception.BusinessValidationException;
import com.studentmanagement.exception.DuplicateResourceException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.exception.ServiceException;
import com.studentmanagement.model.Department;
import com.studentmanagement.model.Student;
import com.studentmanagement.model.enums.Gender;
import com.studentmanagement.repository.DepartmentRepository;
import com.studentmanagement.repository.StudentRepository;
import com.studentmanagement.service.CourseService;
import com.studentmanagement.service.StudentService;
import com.studentmanagement.service.TransactionService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class TestRepo {

    private static final String DEPT_CODE = "CS";
    private static final String COURSE_CODE = "CS101";

    private final StudentService studentService;
    private final CourseService courseService;
    private final TransactionService transactionService;
    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;

    public TestRepo(
            StudentService studentService,
            CourseService courseService,
            TransactionService transactionService,
            DepartmentRepository departmentRepository,
            StudentRepository studentRepository) {
        this.studentService = studentService;
        this.courseService = courseService;
        this.transactionService = transactionService;
        this.departmentRepository = departmentRepository;
        this.studentRepository = studentRepository;
    }

    @PostConstruct
    public void runServiceLayerTests() {
        seedDepartmentAndCourse();
        testRegisterStudent();
        testServiceLayerExceptions();
        testTransactionCommitAndRollback();
    }

    private void testTransactionCommitAndRollback() {
        System.out.println("\n========== TEST: Transaction & Rollback ==========");

        String commitEmail = "txn.commit@example.com";
        String rollbackEmail = "txn.rollback@example.com";

        testTransactionCommit(commitEmail);
        testTransactionRollback(rollbackEmail);
        testTransactionRollbackDuringDelete();
    }

    private void testTransactionCommit(String email) {
        System.out.println("\n--- 1. COMMIT: student + enrollment saved together ---");
        try {
            if (transactionService.studentExistsByEmail(email)) {
                System.out.println("Commit test student already exists, skipping insert");
                return;
            }

            Student saved = transactionService.commitStudentWithEnrollment(
                    "STU_TXN_COMMIT",
                    email,
                    "9111111111",
                    DEPT_CODE,
                    COURSE_CODE,
                    "SEM1",
                    "2025-26");

            System.out.println("COMMIT success");
            System.out.println("   Student ID in DB: " + saved.getId());
            System.out.println("   Email exists: " + transactionService.studentExistsByEmail(email));
            System.out.println("   Enrollments: " + transactionService.countEnrollmentsByStudentId(saved.getId()));

        } catch (Exception e) {
            System.out.println("COMMIT failed: " + e.getMessage());
        }
    }

    private void testTransactionRollback(String email) {
        System.out.println("\n--- 2. ROLLBACK: exception after save → nothing persisted ---");
        boolean before = transactionService.studentExistsByEmail(email);
        System.out.println("   Email exists BEFORE: " + before);

        try {
            transactionService.rollbackAfterStudentSave(
                    "STU_TXN_ROLLBACK",
                    email,
                    "9222222222",
                    DEPT_CODE);
            System.out.println("ROLLBACK test FAILED — no exception thrown");
        } catch (BusinessValidationException e) {
            System.out.println("   Exception caught: " + e.getMessage());
        }

        boolean after = transactionService.studentExistsByEmail(email);
        System.out.println("   Email exists AFTER rollback: " + after);

        if (!after) {
            System.out.println("ROLLBACK verified — student was NOT saved to database");
        } else {
            System.out.println("ROLLBACK FAILED — student still exists in database!");
        }
    }

    private void testTransactionRollbackDuringDelete() {
        System.out.println("\n--- 3. ROLLBACK during delete: enrollments restored ---");

        Student student = studentRepository.findByStudentId("STU_TXN_COMMIT").orElse(null);
        if (student == null) {
            System.out.println("   Skipped — run commit test first to create STU_TXN_COMMIT");
            return;
        }

        long enrollmentsBefore = transactionService.countEnrollmentsByStudentId(student.getId());
        System.out.println("   Enrollments BEFORE: " + enrollmentsBefore);

        try {
            transactionService.rollbackDuringDelete(student.getId());
        } catch (BusinessValidationException e) {
            System.out.println("   Exception caught: " + e.getMessage());
        }

        long enrollmentsAfter = transactionService.countEnrollmentsByStudentId(student.getId());
        System.out.println("   Enrollments AFTER rollback: " + enrollmentsAfter);

        if (enrollmentsBefore == enrollmentsAfter && enrollmentsAfter > 0) {
            System.out.println("ROLLBACK verified — enrollments were NOT deleted");
        } else {
            System.out.println("ROLLBACK check — enrollment count changed unexpectedly");
        }
    }

    private void testRegisterStudent() {
        System.out.println("\n========== TEST: Register Student (Service Layer) ==========");

        StudentRegistrationRequest request = StudentRegistrationRequest.builder()
                .studentId("STU100")
                .firstName("Om")
                .lastName("Patil")
                .email("om.new@example.com")
                .phone("9876543210")
                .dateOfBirth(LocalDate.of(2000, 6, 15))
                .gender(Gender.MALE)
                .departmentCode(DEPT_CODE)
                .courseCode(COURSE_CODE)
                .semester("SEM1")
                .academicYear("2025-26")
                .build();

        try {
            StudentResponse registered = studentService.registerStudent(request);
            printStudent(registered);
            StudentResponse byEmail = studentService.findStudentByEmail(registered.getEmail());
            System.out.println("Verified by findStudentByEmail: " + byEmail.getStudentId());
        } catch (DuplicateResourceException e) {
            handleServiceException("Duplicate on register", e);
            StudentResponse existing = studentService.findStudentByEmail(request.getEmail());
            System.out.println("Existing student: " + existing.getFirstName() + " " + existing.getLastName());
        }
    }

    private void testServiceLayerExceptions() {
        System.out.println("\n========== TEST: Service Layer Exceptions ==========");

        testException(
                "1. BusinessValidationException (age < 18)",
                () -> studentService.registerStudent(StudentRegistrationRequest.builder()
                        .studentId("STU_UNDERAGE")
                        .firstName("Young")
                        .lastName("Student")
                        .email("young@example.com")
                        .phone("9000000001")
                        .dateOfBirth(LocalDate.of(2015, 1, 1))
                        .gender(Gender.MALE)
                        .departmentCode(DEPT_CODE)
                        .courseCode(COURSE_CODE)
                        .semester("SEM1")
                        .academicYear("2025-26")
                        .build()),
                BusinessValidationException.class);

        testException(
                "2. DuplicateResourceException (same email)",
                () -> studentService.registerStudent(StudentRegistrationRequest.builder()
                        .studentId("STU_DUP")
                        .firstName("Duplicate")
                        .lastName("Email")
                        .email("om.new@example.com")
                        .phone("9000000002")
                        .dateOfBirth(LocalDate.of(2000, 1, 1))
                        .gender(Gender.MALE)
                        .departmentCode(DEPT_CODE)
                        .courseCode(COURSE_CODE)
                        .semester("SEM1")
                        .academicYear("2025-26")
                        .build()),
                DuplicateResourceException.class);

        testException(
                "3. ResourceNotFoundException (invalid department)",
                () -> studentService.registerStudent(StudentRegistrationRequest.builder()
                        .studentId("STU_BAD_DEPT")
                        .firstName("Bad")
                        .lastName("Dept")
                        .email("baddept@example.com")
                        .phone("9000000003")
                        .dateOfBirth(LocalDate.of(2000, 1, 1))
                        .gender(Gender.MALE)
                        .departmentCode("INVALID_DEPT")
                        .courseCode(COURSE_CODE)
                        .semester("SEM1")
                        .academicYear("2025-26")
                        .build()),
                ResourceNotFoundException.class);

        testException(
                "4. ResourceNotFoundException (student not found)",
                () -> studentService.findStudentByEmail("notexist@example.com"),
                ResourceNotFoundException.class);
    }

    private void testException(String label, Runnable action, Class<? extends ServiceException> expectedType) {
        try {
            action.run();
            System.out.println(label + " -> FAILED (no exception thrown)");
        } catch (ServiceException e) {
            if (expectedType.isInstance(e)) {
                System.out.println(label + " -> CAUGHT OK");
                System.out.println("   Type: " + e.getClass().getSimpleName());
                System.out.println("   ErrorCode: " + e.getErrorCode());
                System.out.println("   Message: " + e.getMessage());
            } else {
                System.out.println(label + " -> WRONG TYPE: " + e.getClass().getSimpleName());
            }
        }
    }

    private void handleServiceException(String context, ServiceException e) {
        System.out.println(context);
        System.out.println("   Type: " + e.getClass().getSimpleName());
        System.out.println("   ErrorCode: " + e.getErrorCode());
        System.out.println("   Message: " + e.getMessage());
    }

    private void printStudent(StudentResponse registered) {
        System.out.println("=== Student Registered ===");
        System.out.println("ID: " + registered.getId());
        System.out.println("Student ID: " + registered.getStudentId());
        System.out.println("Name: " + registered.getFirstName() + " " + registered.getLastName());
        System.out.println("Email: " + registered.getEmail());
        System.out.println("Department: " + registered.getDepartmentCode());
        System.out.println("Status: " + registered.getStatus());
    }

    private void seedDepartmentAndCourse() {
        if (!departmentRepository.existsByCode(DEPT_CODE)) {
            Department department = Department.builder()
                    .code(DEPT_CODE)
                    .name("Computer Science")
                    .description("Computer Science Department")
                    .headOfDepartment("Dr. Smith")
                    .build();
            departmentRepository.save(department);
            System.out.println("Seeded department: " + DEPT_CODE);
        }

        try {
            courseService.createCourse(CourseCreateRequest.builder()
                    .courseCode(COURSE_CODE)
                    .courseName("Introduction to Programming")
                    .description("Basics of Java programming")
                    .credits(4)
                    .semester("SEM1")
                    .academicYear("2025-26")
                    .departmentCode(DEPT_CODE)
                    .build());
            System.out.println("Seeded course: " + COURSE_CODE);
        } catch (DuplicateResourceException e) {
            System.out.println("Course already exists: " + COURSE_CODE);
        }
    }
}
