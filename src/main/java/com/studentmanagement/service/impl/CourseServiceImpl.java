package com.studentmanagement.service.impl;

import com.studentmanagement.dto.request.CourseCreateRequest;
import com.studentmanagement.exception.DuplicateResourceException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.model.Course;
import com.studentmanagement.model.Department;
import com.studentmanagement.model.Instructor;
import com.studentmanagement.repository.CourseRepository;
import com.studentmanagement.repository.DepartmentRepository;
import com.studentmanagement.repository.InstructorRepository;
import com.studentmanagement.service.CourseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final InstructorRepository instructorRepository;

    public CourseServiceImpl(
            CourseRepository courseRepository,
            DepartmentRepository departmentRepository,
            InstructorRepository instructorRepository) {
        this.courseRepository = courseRepository;
        this.departmentRepository = departmentRepository;
        this.instructorRepository = instructorRepository;
    }

    @Override
    @Transactional
    public Course createCourse(CourseCreateRequest request) {
        if (courseRepository.existsByCourseCode(request.getCourseCode())) {
            throw new DuplicateResourceException("Course code already exists: " + request.getCourseCode());
        }

        Department department = departmentRepository.findByCode(request.getDepartmentCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with code: " + request.getDepartmentCode()));

        Course.CourseBuilder courseBuilder = Course.builder()
                .courseCode(request.getCourseCode())
                .courseName(request.getCourseName())
                .description(request.getDescription())
                .credits(request.getCredits())
                .semester(request.getSemester())
                .academicYear(request.getAcademicYear())
                .department(department);

        if (StringUtils.hasText(request.getInstructorEmployeeId())) {
            Instructor instructor = instructorRepository.findByEmployeeId(request.getInstructorEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Instructor not found with employee ID: " + request.getInstructorEmployeeId()));
            courseBuilder.instructor(instructor);
        }

        return courseRepository.save(courseBuilder.build());
    }

    @Override
    @Transactional
    public Course updateCourse(Long id, CourseCreateRequest request) {
        Course course = getCourseById(id);

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
            Department department = departmentRepository.findByCode(request.getDepartmentCode())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department not found with code: " + request.getDepartmentCode()));
            course.setDepartment(department);
        }
        if (StringUtils.hasText(request.getInstructorEmployeeId())) {
            Instructor instructor = instructorRepository.findByEmployeeId(request.getInstructorEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Instructor not found with employee ID: " + request.getInstructorEmployeeId()));
            course.setInstructor(instructor);
        }

        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public void deleteCourseById(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
    }

    @Override
    public Course findCourseById(Long id) {
        return getCourseById(id);
    }

    @Override
    public Course findCourseByCode(String courseCode) {
        return courseRepository.findByCourseCode(courseCode)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with code: " + courseCode));
    }

    @Override
    public List<Course> searchCourse(String keyword) {
        return courseRepository.findByCourseNameContainingIgnoreCase(keyword);
    }

    @Override
    @Transactional
    public Course assignInstructorToCourse(String courseCode, String instructorEmployeeId) {
        Course course = findCourseByCode(courseCode);
        Instructor instructor = instructorRepository.findByEmployeeId(instructorEmployeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Instructor not found with employee ID: " + instructorEmployeeId));

        course.setInstructor(instructor);
        return courseRepository.save(course);
    }

    private Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }
}
