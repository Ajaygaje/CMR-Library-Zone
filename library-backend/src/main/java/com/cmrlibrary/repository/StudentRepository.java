
package com.cmrlibrary.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cmrlibrary.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {

    // Search students by name
    List<Student> findByNameContainingIgnoreCase(String name);

    // Find student using roll number
    Student findByRollNumber(String rollNumber);

    // Find student using email
    Student findByEmail(String email);
}
