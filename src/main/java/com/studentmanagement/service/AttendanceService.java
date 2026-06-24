package com.studentmanagement.service;

import com.studentmanagement.dto.request.AttendanceMarkRequest;
import com.studentmanagement.dto.response.AttendancePercentageResponse;
import com.studentmanagement.dto.response.AttendanceResponse;
import com.studentmanagement.exception.BusinessValidationException;
import com.studentmanagement.exception.DuplicateResourceException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.exception.ServiceException;
import com.studentmanagement.model.Attendance;
import com.studentmanagement.model.Course;
import com.studentmanagement.model.Student;
import com.studentmanagement.model.enums.AttendanceStatus;
import com.studentmanagement.model.enums.EnrollmentStatus;
import com.studentmanagement.repository.AttendanceRepository;
import com.studentmanagement.repository.CourseRepository;
import com.studentmanagement.repository.EnrollmentRepository;
import com.studentmanagement.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            StudentRepository studentRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional(rollbackFor = ServiceException.class)
    public AttendanceResponse markAttendance(AttendanceMarkRequest request) {
        Student student = findStudentByStudentId(request.getStudentId());
        Course course = findCourseByCode(request.getCourseCode());

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

        return toResponse(attendanceRepository.save(attendance));
    }

    public List<AttendanceResponse> getAttendanceByStudent(String studentId) {
        Student student = findStudentByStudentId(studentId);
        return attendanceRepository.findByStudent_Id(student.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AttendanceResponse> getAttendanceByCourse(String courseCode) {
        Course course = findCourseByCode(courseCode);
        return attendanceRepository.findByCourse_Id(course.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    public AttendancePercentageResponse getAttendancePercentage(String studentId, String courseCode) {
        Student student = findStudentByStudentId(studentId);
        Course course = findCourseByCode(courseCode);

        List<Attendance> records = attendanceRepository.findByStudent_IdAndCourse_Id(
                student.getId(), course.getId());

        long totalSessions = records.size();
        long presentCount = attendanceRepository.countByStudent_IdAndCourse_IdAndStatus(
                student.getId(), course.getId(), AttendanceStatus.PRESENT);

        double percentage = totalSessions == 0 ? 0.0 : (presentCount * 100.0) / totalSessions;

        return AttendancePercentageResponse.builder()
                .studentId(studentId)
                .courseCode(courseCode)
                .percentage(percentage)
                .totalSessions(totalSessions)
                .presentCount(presentCount)
                .build();
    }

    private Student findStudentByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with ID: " + studentId));
    }

    private Course findCourseByCode(String courseCode) {
        return courseRepository.findByCourseCode(courseCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found with code: " + courseCode));
    }

    private void validateStudentEnrolledInCourse(Long studentId, Long courseId) {
        boolean enrolled = enrollmentRepository.findByStudent_Id(studentId).stream()
                .anyMatch(e -> e.getCourse().getId().equals(courseId)
                        && e.getStatus() != EnrollmentStatus.DROPPED);

        if (!enrolled) {
            throw new BusinessValidationException("Student is not enrolled in this course");
        }
    }

    private AttendanceResponse toResponse(Attendance attendance) {
        AttendanceResponse.AttendanceResponseBuilder builder = AttendanceResponse.builder()
                .id(attendance.getId())
                .attendanceDate(attendance.getAttendanceDate())
                .status(attendance.getStatus())
                .remarks(attendance.getRemarks());

        if (attendance.getStudent() != null) {
            Student student = attendance.getStudent();
            builder.studentId(student.getStudentId())
                    .studentName(student.getFirstName() + " " + student.getLastName());
        }

        if (attendance.getCourse() != null) {
            Course course = attendance.getCourse();
            builder.courseCode(course.getCourseCode())
                    .courseName(course.getCourseName());
        }

        return builder.build();
    }
}
