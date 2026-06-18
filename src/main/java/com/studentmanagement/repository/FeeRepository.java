package com.studentmanagement.repository;

import com.studentmanagement.model.Fee;
import com.studentmanagement.model.enums.FeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeRepository extends JpaRepository<Fee, Long> {

    List<Fee> findByStudent_Id(Long studentId);

    List<Fee> findByStudent_IdAndStatus(Long studentId, FeeStatus status);

    List<Fee> findByStudent_StudentId(String studentId);

    long countByStudent_IdAndStatus(Long studentId, FeeStatus status);
}
