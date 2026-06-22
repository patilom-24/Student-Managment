package com.studentmanagement.pac;

import com.studentmanagement.dto.request.CourseCreateRequest;
import com.studentmanagement.dto.request.StudentRegistrationRequest;
import com.studentmanagement.dto.response.StudentResponse;
import com.studentmanagement.exception.BusinessValidationException;
import com.studentmanagement.exception.DuplicateResourceException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.exception.ServiceException;
import com.studentmanagement.model.Department;
import com.studentmanagement.model.enums.Gender;
import com.studentmanagement.repository.DepartmentRepository;
import com.studentmanagement.service.CourseService;
import com.studentmanagement.service.StudentService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class TestRepo {

    private static final String DEPT_CODE = "CS";
    private static final String COURSE_CODE = "CS101";

    private final StudentService studentService;
    private final CourseService courseService;
    private final DepartmentRepository departmentRepository;

    public TestRepo(
            StudentService studentService,
            CourseService courseService,
            DepartmentRepository departmentRepository) {
        this.studentService = studentService;
        this.courseService = courseService;
        this.departmentRepository = departmentRepository;
    }

    @PostConstruct
    public void runServiceLayerTests() {
        seedDepartmentAndCourse();
        testRegisterStudent();
        testServiceLayerExceptions();
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
