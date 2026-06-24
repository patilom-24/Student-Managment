package com.studentmanagement.service;

import com.studentmanagement.dto.request.AssignInstructorRequest;
import com.studentmanagement.dto.request.CourseCreateRequest;
import com.studentmanagement.dto.request.CourseUpdateRequest;
import com.studentmanagement.dto.response.CourseResponse;
import com.studentmanagement.exception.DuplicateResourceException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.exception.ServiceException;
import com.studentmanagement.model.Course;
import com.studentmanagement.model.Department;
import com.studentmanagement.model.Instructor;
import com.studentmanagement.repository.CourseRepository;
import com.studentmanagement.repository.DepartmentRepository;
import com.studentmanagement.repository.InstructorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final InstructorRepository instructorRepository;

    public CourseService(
            CourseRepository courseRepository,
            DepartmentRepository departmentRepository,
            InstructorRepository instructorRepository) {
        this.courseRepository = courseRepository;
        this.departmentRepository = departmentRepository;
        this.instructorRepository = instructorRepository;
    }

    @Transactional(rollbackFor = ServiceException.class)
    public CourseResponse createCourse(CourseCreateRequest request) {
        if (courseRepository.existsByCourseCode(request.getCourseCode())) {
            throw new DuplicateResourceException("Course code already exists: " + request.getCourseCode());
        }

        Department department = findDepartmentByCode(request.getDepartmentCode());

        Course.CourseBuilder courseBuilder = Course.builder()
                .courseCode(request.getCourseCode())
                .courseName(request.getCourseName())
                .description(request.getDescription())
                .credits(request.getCredits())
                .semester(request.getSemester())
                .academicYear(request.getAcademicYear())
                .department(department);

        if (StringUtils.hasText(request.getInstructorEmployeeId())) {
            courseBuilder.instructor(findInstructorByEmployeeId(request.getInstructorEmployeeId()));
        }

        return toResponse(courseRepository.save(courseBuilder.build()));
    }

    @Transactional(rollbackFor = ServiceException.class)
    public CourseResponse updateCourse(Long id, CourseUpdateRequest request) {
        Course course = getCourseEntityById(id);

        if (StringUtils.hasText(request.getCourseCode())
                && !request.getCourseCode().equals(course.getCourseCode())
                && courseRepository.existsByCourseCode(request.getCourseCode())) {
            throw new DuplicateResourceException("Course code already exists: " + request.getCourseCode());
        }

        if (StringUtils.hasText(request.getCourseCode())) {
            course.setCourseCode(request.getCourseCode());
        }
        if (StringUtils.hasText(request.getCourseName())) {
            course.setCourseName(request.getCourseName());
        }
        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }
        if (request.getCredits() != null) {
            course.setCredits(request.getCredits());
        }
        if (StringUtils.hasText(request.getSemester())) {
            course.setSemester(request.getSemester());
        }
        if (StringUtils.hasText(request.getAcademicYear())) {
            course.setAcademicYear(request.getAcademicYear());
        }
        if (StringUtils.hasText(request.getDepartmentCode())) {
            course.setDepartment(findDepartmentByCode(request.getDepartmentCode()));
        }
        if (StringUtils.hasText(request.getInstructorEmployeeId())) {
            course.setInstructor(findInstructorByEmployeeId(request.getInstructorEmployeeId()));
        }

        return toResponse(courseRepository.save(course));
    }

    @Transactional(rollbackFor = ServiceException.class)
    public void deleteCourseById(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
    }

    public CourseResponse findCourseById(Long id) {
        return toResponse(getCourseEntityById(id));
    }

    public CourseResponse findCourseByCode(String courseCode) {
        return courseRepository.findByCourseCode(courseCode)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with code: " + courseCode));
    }

    public List<CourseResponse> searchCourse(String keyword) {
        return courseRepository.findByCourseNameContainingIgnoreCase(keyword).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(rollbackFor = ServiceException.class)
    public CourseResponse assignInstructorToCourse(AssignInstructorRequest request) {
        Course course = courseRepository.findByCourseCode(request.getCourseCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found with code: " + request.getCourseCode()));
        Instructor instructor = findInstructorByEmployeeId(request.getInstructorEmployeeId());

        course.setInstructor(instructor);
        return toResponse(courseRepository.save(course));
    }

    private Department findDepartmentByCode(String departmentCode) {
        return departmentRepository.findByCode(departmentCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with code: " + departmentCode));
    }

    private Instructor findInstructorByEmployeeId(String employeeId) {
        return instructorRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Instructor not found with employee ID: " + employeeId));
    }

    private Course getCourseEntityById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    private CourseResponse toResponse(Course course) {
        CourseResponse.CourseResponseBuilder builder = CourseResponse.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .description(course.getDescription())
                .credits(course.getCredits())
                .semester(course.getSemester())
                .academicYear(course.getAcademicYear());

        if (course.getDepartment() != null) {
            builder.departmentCode(course.getDepartment().getCode())
                    .departmentName(course.getDepartment().getName());
        }

        if (course.getInstructor() != null) {
            Instructor instructor = course.getInstructor();
            builder.instructorEmployeeId(instructor.getEmployeeId())
                    .instructorName(instructor.getFirstName() + " " + instructor.getLastName());
        }

        return builder.build();
    }
}
