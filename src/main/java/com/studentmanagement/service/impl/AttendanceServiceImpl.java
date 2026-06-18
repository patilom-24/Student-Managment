package com.studentmanagement.service.impl;

import com.studentmanagement.dto.request.AttendanceMarkRequest;
import com.studentmanagement.exception.BusinessValidationException;
import com.studentmanagement.exception.DuplicateResourceException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.model.Attendance;
import com.studentmanagement.model.Course;
import com.studentmanagement.model.Student;
import com.studentmanagement.model.enums.AttendanceStatus;
import com.studentmanagement.model.enums.EnrollmentStatus;
import com.studentmanagement.repository.AttendanceRepository;
import com.studentmanagement.repository.CourseRepository;
import com.studentmanagement.repository.EnrollmentRepository;
import com.studentmanagement.repository.StudentRepository;
import com.studentmanagement.service.AttendanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public AttendanceServiceImpl(
            AttendanceRepository attendanceRepository,
            StudentRepository studentRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    @Transactional
    public Attendance markAttendance(AttendanceMarkRequest request) {
        Student student = studentRepository.findByStudentId(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with ID: " + request.getStudentId()));

        Course course = courseRepository.findByCourseCode(request.getCourseCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found with code: " + request.getCourseCode()));

        validateStudentEnrolledInCourse(student.getId(), course.getId());

        if (attendanceRepository.findByStudent_IdAndCourse_IdAndAttendanceDate(
                student.getId(), course.getId(), request.getAttendanceDate()).isPresent()) {
            throw new DuplicateResourceException("Attendance already marked for this student, course, and date");
        }

        Attendance attendance = Attendance.builder()
                .student(student)
                .course(course)
                .attendanceDate(request.getAttendanceDate())
                .status(request.getStatus())
                .remarks(request.getRemarks())
                .build();

        return attendanceRepository.save(attendance);
    }

    @Override
    public List<Attendance> getAttendanceByStudent(String studentId) {
        Student student = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with ID: " + studentId));
        return attendanceRepository.findByStudent_Id(student.getId());
    }

    @Override
    public List<Attendance> getAttendanceByCourse(String courseCode) {
        Course course = courseRepository.findByCourseCode(courseCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found with code: " + courseCode));
        return attendanceRepository.findByCourse_Id(course.getId());
    }

    @Override
    public double getAttendancePercentage(String studentId, String courseCode) {
        Student student = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with ID: " + studentId));
        Course course = courseRepository.findByCourseCode(courseCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found with code: " + courseCode));

        List<Attendance> records = attendanceRepository.findByStudent_IdAndCourse_Id(
                student.getId(), course.getId());

        if (records.isEmpty()) {
            return 0.0;
        }

        long presentCount = attendanceRepository.countByStudent_IdAndCourse_IdAndStatus(
                student.getId(), course.getId(), AttendanceStatus.PRESENT);

        return (presentCount * 100.0) / records.size();
    }

    private void validateStudentEnrolledInCourse(Long studentId, Long courseId) {
        boolean enrolled = enrollmentRepository.findByStudent_Id(studentId).stream()
                .anyMatch(e -> e.getCourse().getId().equals(courseId)
                        && e.getStatus() != EnrollmentStatus.DROPPED);

        if (!enrolled) {
            throw new BusinessValidationException("Student is not enrolled in this course");
        }
    }
}
