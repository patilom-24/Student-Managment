package com.studentmanagement.service;

import com.studentmanagement.dto.request.FacultyRegistrationRequest;
import com.studentmanagement.dto.request.FacultyUpdateRequest;
import com.studentmanagement.dto.response.FacultyResponse;
import com.studentmanagement.exception.DuplicateResourceException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.exception.ServiceException;
import com.studentmanagement.model.Department;
import com.studentmanagement.model.Instructor;
import com.studentmanagement.repository.DepartmentRepository;
import com.studentmanagement.repository.InstructorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class FacultyService {

    private final InstructorRepository instructorRepository;
    private final DepartmentRepository departmentRepository;

    public FacultyService(
            InstructorRepository instructorRepository,
            DepartmentRepository departmentRepository) {
        this.instructorRepository = instructorRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional(rollbackFor = ServiceException.class)
    public FacultyResponse registerFaculty(FacultyRegistrationRequest request) {
        if (instructorRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Faculty email already registered: " + request.getEmail());
        }
        if (instructorRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new DuplicateResourceException("Employee ID already exists: " + request.getEmployeeId());
        }

        Department department = findDepartmentByCode(request.getDepartmentCode());

        Instructor instructor = Instructor.builder()
                .employeeId(request.getEmployeeId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .specialization(request.getSpecialization())
                .department(department)
                .build();

        return toResponse(instructorRepository.save(instructor));
    }

    @Transactional(rollbackFor = ServiceException.class)
    public FacultyResponse updateFaculty(Long id, FacultyUpdateRequest request) {
        Instructor instructor = getInstructorEntityById(id);

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
            instructor.setDepartment(findDepartmentByCode(request.getDepartmentCode()));
        }

        return toResponse(instructorRepository.save(instructor));
    }

    @Transactional(rollbackFor = ServiceException.class)
    public void deleteFacultyById(Long id) {
        if (!instructorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Faculty not found with id: " + id);
        }
        instructorRepository.deleteById(id);
    }

    public FacultyResponse findFacultyById(Long id) {
        return toResponse(getInstructorEntityById(id));
    }

    public FacultyResponse findFacultyByEmail(String email) {
        return instructorRepository.findByEmail(email)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with email: " + email));
    }

    public List<FacultyResponse> searchFaculty(String keyword) {
        return instructorRepository.searchByName(keyword).stream()
                .map(this::toResponse)
                .toList();
    }

    private Department findDepartmentByCode(String departmentCode) {
        return departmentRepository.findByCode(departmentCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with code: " + departmentCode));
    }

    private Instructor getInstructorEntityById(Long id) {
        return instructorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + id));
    }

    private FacultyResponse toResponse(Instructor instructor) {
        FacultyResponse.FacultyResponseBuilder builder = FacultyResponse.builder()
                .id(instructor.getId())
                .employeeId(instructor.getEmployeeId())
                .firstName(instructor.getFirstName())
                .lastName(instructor.getLastName())
                .email(instructor.getEmail())
                .phone(instructor.getPhone())
                .specialization(instructor.getSpecialization());

        if (instructor.getDepartment() != null) {
            builder.departmentCode(instructor.getDepartment().getCode())
                    .departmentName(instructor.getDepartment().getName());
        }

        return builder.build();
    }
}
