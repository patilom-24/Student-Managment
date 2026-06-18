package com.studentmanagement.service;

import com.studentmanagement.dto.request.FacultyRegistrationRequest;
import com.studentmanagement.model.Instructor;

import java.util.List;

public interface FacultyService {

    Instructor registerFaculty(FacultyRegistrationRequest request);

    Instructor updateFaculty(Long id, FacultyRegistrationRequest request);

    void deleteFacultyById(Long id);

    Instructor findFacultyById(Long id);

    Instructor findFacultyByEmail(String email);

    List<Instructor> searchFaculty(String keyword);
}
