package com.studentmanagement.repository;

import com.studentmanagement.model.Attendance;
import com.studentmanagement.model.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByStudent_Id(Long studentId);

    List<Attendance> findByCourse_Id(Long courseId);

    List<Attendance> findByStudent_IdAndCourse_Id(Long studentId, Long courseId);

    Optional<Attendance> findByStudent_IdAndCourse_IdAndAttendanceDate(
            Long studentId, Long courseId, LocalDate attendanceDate);

    long countByStudent_IdAndCourse_IdAndStatus(Long studentId, Long courseId, AttendanceStatus status);
}
