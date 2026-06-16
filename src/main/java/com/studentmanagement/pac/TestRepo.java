package com.studentmanagement.pac;

import com.studentmanagement.model.Student;
import com.studentmanagement.model.enums.StudentStatus;
import com.studentmanagement.repository.AddressRepository;
import com.studentmanagement.repository.StudentRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class TestRepo {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private StudentRepository studentRepository;

    @PostConstruct
    public void testCustomQueries() {
        // 1. Derived query
        Student byEmail = studentRepository.findStudentByEmail("om3.patil@example.com");
        if (byEmail != null) {
            System.out.println("By email: " + byEmail.getFirstName());

            // 2. Derived query with AND
            List<Student> active = studentRepository.findByStatusAndGender(
                    StudentStatus.ACTIVE, byEmail.getGender());
            System.out.println("Active with same gender: " + active.size());
        }

        // 3. Derived query with contains
        List<Student> byName = studentRepository.findByFirstNameContainingIgnoreCase("om");

        // 4. JPQL custom query
        List<Student> recent = studentRepository.findEnrolledOnOrAfter(LocalDate.now().minusYears(1));

        // 5. JPQL join query
        List<Student> inCity = studentRepository.findByAddressCity("Mumbai");

        // 6. Native query
        List<Student> nativeResult = studentRepository.findByStatusNative("ACTIVE");

        // 7. Pagination
        var page = studentRepository.findByStatus(StudentStatus.ACTIVE, PageRequest.of(0, 10));

        System.out.println("Name search: " + byName.size());
        System.out.println("Recent enrollments: " + recent.size());
        System.out.println("Students in Mumbai: " + inCity.size());
        System.out.println("Native query count: " + nativeResult.size());
        System.out.println("Page total elements: " + page.getTotalElements());
    }
}
