package com.studentmanagement.service;

import com.studentmanagement.dto.request.AttendanceMarkRequest;
import com.studentmanagement.model.Attendance;

import java.util.List;

public interface AttendanceService {

    Attendance markAttendance(AttendanceMarkRequest request);

    List<Attendance> getAttendanceByStudent(String studentId);

    List<Attendance> getAttendanceByCourse(String courseCode);

    double getAttendancePercentage(String studentId, String courseCode);
}
