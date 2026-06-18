package com.studentmanagement.service;

import com.studentmanagement.dto.request.StudentRegistrationRequest;
import com.studentmanagement.dto.request.StudentUpdateRequest;
import com.studentmanagement.dto.response.StudentResponse;

import java.util.List;

public interface StudentService {

    StudentResponse registerStudent(StudentRegistrationRequest request);

    StudentResponse updateStudent(Long id, StudentUpdateRequest request);

    void deleteStudentById(Long id);

    StudentResponse findStudentById(Long id);

    StudentResponse findStudentByEmail(String email);

    List<StudentResponse> searchStudent(String keyword);

    StudentResponse getStudentProfile(Long id);
}
