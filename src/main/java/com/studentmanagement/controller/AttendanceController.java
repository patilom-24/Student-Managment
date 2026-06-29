package com.studentmanagement.controller;

import com.studentmanagement.dto.request.AttendanceMarkRequest;
import com.studentmanagement.dto.response.ApiResponse;
import com.studentmanagement.dto.response.AttendancePercentageResponse;
import com.studentmanagement.dto.response.AttendanceResponse;
import com.studentmanagement.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AttendanceResponse>> markAttendance(
            @Valid @RequestBody AttendanceMarkRequest request) {
        AttendanceResponse response = attendanceService.markAttendance(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attendance marked successfully", response));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAttendanceByStudent(
            @PathVariable String studentId) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getAttendanceByStudent(studentId)));
    }

    @GetMapping("/course/{courseCode}")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAttendanceByCourse(
            @PathVariable String courseCode) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getAttendanceByCourse(courseCode)));
    }

    @GetMapping("/percentage")
    public ResponseEntity<ApiResponse<AttendancePercentageResponse>> getAttendancePercentage(
            @RequestParam String studentId,
            @RequestParam String courseCode) {
        return ResponseEntity.ok(ApiResponse.success(
                attendanceService.getAttendancePercentage(studentId, courseCode)));
    }
}
