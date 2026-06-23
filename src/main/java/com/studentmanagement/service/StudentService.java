package com.studentmanagement.service;

import com.studentmanagement.dto.request.StudentRegistrationRequest;
import com.studentmanagement.dto.request.StudentUpdateRequest;
import com.studentmanagement.dto.response.StudentResponse;
import com.studentmanagement.exception.BusinessValidationException;
import com.studentmanagement.exception.DuplicateResourceException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.exception.ServiceException;
import com.studentmanagement.model.Course;
import com.studentmanagement.model.Department;
import com.studentmanagement.model.Enrollment;
import com.studentmanagement.model.Student;
import com.studentmanagement.model.enums.EnrollmentStatus;
import com.studentmanagement.model.enums.StudentStatus;
import com.studentmanagement.repository.CourseRepository;
import com.studentmanagement.repository.DepartmentRepository;
import com.studentmanagement.repository.EnrollmentRepository;
import com.studentmanagement.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class StudentService {

    private static final int MINIMUM_AGE = 18;

    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public StudentService(
            StudentRepository studentRepository,
            DepartmentRepository departmentRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository) {
        this.studentRepository = studentRepository;
        this.departmentRepository = departmentRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional(rollbackFor = ServiceException.class)
    public StudentResponse registerStudent(StudentRegistrationRequest request) {
        validateEmailUnique(request.getEmail());
        validateMobileUnique(request.getPhone());
        validateAge(request.getDateOfBirth());

        Department department = findDepartmentByCode(request.getDepartmentCode());
        Course course = findCourseByCode(request.getCourseCode());

        if (studentRepository.existsByStudentId(request.getStudentId())) {
            throw new DuplicateResourceException("Student ID already exists: " + request.getStudentId());
        }

        Student student = Student.builder()
                .studentId(request.getStudentId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .status(StudentStatus.ACTIVE)
                .department(department)
                .build();

        Student savedStudent = studentRepository.save(student);

        Enrollment enrollment = Enrollment.builder()
                .student(savedStudent)
                .course(course)
                .semester(request.getSemester())
                .academicYear(request.getAcademicYear())
                .status(EnrollmentStatus.ENROLLED)
                .build();

        enrollmentRepository.save(enrollment);

        return toResponse(savedStudent);
    }

    @Transactional(rollbackFor = ServiceException.class)
    public StudentResponse updateStudent(Long id, StudentUpdateRequest request) {
        Student student = getStudentEntityById(id);

        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(student.getEmail())) {
            validateEmailUnique(request.getEmail());
            student.setEmail(request.getEmail());
        }

        if (StringUtils.hasText(request.getPhone()) && !request.getPhone().equals(student.getPhone())) {
            validateMobileUnique(request.getPhone());
            student.setPhone(request.getPhone());
        }

        if (request.getDateOfBirth() != null) {
            validateAge(request.getDateOfBirth());
            student.setDateOfBirth(request.getDateOfBirth());
        }

        if (StringUtils.hasText(request.getFirstName())) {
            student.setFirstName(request.getFirstName());
        }
        if (StringUtils.hasText(request.getLastName())) {
            student.setLastName(request.getLastName());
        }
        if (request.getGender() != null) {
            student.setGender(request.getGender());
        }
        if (request.getStatus() != null) {
            student.setStatus(request.getStatus());
        }
        if (StringUtils.hasText(request.getDepartmentCode())) {
            student.setDepartment(findDepartmentByCode(request.getDepartmentCode()));
        }

        return toResponse(studentRepository.save(student));
    }

    @Transactional(rollbackFor = ServiceException.class)
    public void deleteStudentById(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student not found with id: " + id);
        }
        studentRepository.deleteById(id);
    }

    public StudentResponse findStudentById(Long id) {
        return toResponse(getStudentEntityById(id));
    }

    public StudentResponse findStudentByEmail(String email) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with email: " + email));
        return toResponse(student);
    }

    public List<StudentResponse> searchStudent(String keyword) {
        return studentRepository.searchByKeyword(keyword).stream()
                .map(this::toResponse)
                .toList();
    }

    public StudentResponse getStudentProfile(Long id) {
        return findStudentById(id);
    }

    private void validateEmailUnique(String email) {
        if (studentRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already registered: " + email);
        }
    }

    private void validateMobileUnique(String phone) {
        if (StringUtils.hasText(phone) && studentRepository.existsByPhone(phone)) {
            throw new DuplicateResourceException("Mobile number already registered: " + phone);
        }
    }

    private void validateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            throw new BusinessValidationException("Date of birth is required");
        }
        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        if (age < MINIMUM_AGE) {
            throw new BusinessValidationException("Student must be at least " + MINIMUM_AGE + " years old");
        }
    }

    private Department findDepartmentByCode(String departmentCode) {
        return departmentRepository.findByCode(departmentCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department/Batch not found with code: " + departmentCode));
    }

    private Course findCourseByCode(String courseCode) {
        return courseRepository.findByCourseCode(courseCode)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with code: " + courseCode));
    }

    private Student getStudentEntityById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    private StudentResponse toResponse(Student student) {
        StudentResponse.StudentResponseBuilder builder = StudentResponse.builder()
                .id(student.getId())
                .studentId(student.getStudentId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .email(student.getEmail())
                .phone(student.getPhone())
                .dateOfBirth(student.getDateOfBirth())
                .gender(student.getGender())
                .status(student.getStatus())
                .enrollmentDate(student.getEnrollmentDate());

        if (student.getDepartment() != null) {
            builder.departmentCode(student.getDepartment().getCode())
                    .departmentName(student.getDepartment().getName());
        }

        return builder.build();
    }
}
