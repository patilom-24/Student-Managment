package com.studentmanagement.service.impl;

import com.studentmanagement.dto.request.FacultyRegistrationRequest;
import com.studentmanagement.exception.DuplicateResourceException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.model.Department;
import com.studentmanagement.model.Instructor;
import com.studentmanagement.repository.DepartmentRepository;
import com.studentmanagement.repository.InstructorRepository;
import com.studentmanagement.service.FacultyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class FacultyServiceImpl implements FacultyService {

    private final InstructorRepository instructorRepository;
    private final DepartmentRepository departmentRepository;

    public FacultyServiceImpl(
            InstructorRepository instructorRepository,
            DepartmentRepository departmentRepository) {
        this.instructorRepository = instructorRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    @Transactional
    public Instructor registerFaculty(FacultyRegistrationRequest request) {
        if (instructorRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Faculty email already registered: " + request.getEmail());
        }
        if (instructorRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new DuplicateResourceException("Employee ID already exists: " + request.getEmployeeId());
        }

        Department department = departmentRepository.findByCode(request.getDepartmentCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with code: " + request.getDepartmentCode()));

        Instructor instructor = Instructor.builder()
                .employeeId(request.getEmployeeId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .specialization(request.getSpecialization())
                .department(department)
                .build();

        return instructorRepository.save(instructor);
    }

    @Override
    @Transactional
    public Instructor updateFaculty(Long id, FacultyRegistrationRequest request) {
        Instructor instructor = getInstructorById(id);

        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(instructor.getEmail())) {
            if (instructorRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Faculty email already registered: " + request.getEmail());
            }
            instructor.setEmail(request.getEmail());
        }

        if (StringUtils.hasText(request.getFirstName())) {
            instructor.setFirstName(request.getFirstName());
        }
        if (StringUtils.hasText(request.getLastName())) {
            instructor.setLastName(request.getLastName());
        }
        if (StringUtils.hasText(request.getPhone())) {
            instructor.setPhone(request.getPhone());
        }
        if (request.getSpecialization() != null) {
            instructor.setSpecialization(request.getSpecialization());
        }
        if (StringUtils.hasText(request.getDepartmentCode())) {
            Department department = departmentRepository.findByCode(request.getDepartmentCode())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department not found with code: " + request.getDepartmentCode()));
            instructor.setDepartment(department);
        }

        return instructorRepository.save(instructor);
    }

    @Override
    @Transactional
    public void deleteFacultyById(Long id) {
        if (!instructorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Faculty not found with id: " + id);
        }
        instructorRepository.deleteById(id);
    }

    @Override
    public Instructor findFacultyById(Long id) {
        return getInstructorById(id);
    }

    @Override
    public Instructor findFacultyByEmail(String email) {
        return instructorRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with email: " + email));
    }

    @Override
    public List<Instructor> searchFaculty(String keyword) {
        return instructorRepository.searchByName(keyword);
    }

    private Instructor getInstructorById(Long id) {
        return instructorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + id));
    }
}
