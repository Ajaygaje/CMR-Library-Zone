
package com.cmrlibrary.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cmrlibrary.entity.Student;
import com.cmrlibrary.repository.StudentRepository;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentRepository studentRepository;

    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // STUDENT: View own profile
    @GetMapping("/profile")
    public ResponseEntity<?> getMyProfile(
            Authentication authentication) {

        String email = authentication.getName();

        Student student = studentRepository.findByEmail(email);

        if (student == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Student profile not found");
        }

        return ResponseEntity.ok(student);
    }

    // STUDENT: Update only their own phone number
    @PutMapping("/profile")
    public ResponseEntity<?> updateMyProfile(
            Authentication authentication,
            @RequestBody Map<String, String> request) {

        String email = authentication.getName();

        Student student = studentRepository.findByEmail(email);

        if (student == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Student profile not found");
        }

        String phone = request.get("phone");

        if (phone == null || phone.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Phone number cannot be empty");
        }

        phone = phone.trim();

        if (phone.length() > 20) {
            return ResponseEntity.badRequest()
                    .body("Phone number is too long");
        }

        student.setPhone(phone);

        Student updatedStudent = studentRepository.save(student);

        return ResponseEntity.ok(updatedStudent);
    }

    // ADMIN / LIBRARIAN: Get all students
    @GetMapping
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // ADMIN / LIBRARIAN: Add student
    @PostMapping
    public Student addStudent(@RequestBody Student student) {
        return studentRepository.save(student);
    }

    // ADMIN / LIBRARIAN: Update student
    @PutMapping("/{id}")
    public Student updateStudent(
            @PathVariable Long id,
            @RequestBody Student updatedStudent) {

        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        existingStudent.setName(updatedStudent.getName());
        existingStudent.setRollNumber(updatedStudent.getRollNumber());
        existingStudent.setEmail(updatedStudent.getEmail());
        existingStudent.setDepartment(updatedStudent.getDepartment());
        existingStudent.setPhone(updatedStudent.getPhone());

        return studentRepository.save(existingStudent);
    }

    // ADMIN / LIBRARIAN: Delete student
    @DeleteMapping("/{id}")
    public String deleteStudent(@PathVariable Long id) {

        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        studentRepository.delete(existingStudent);

        return "Student deleted successfully!";
    }

    // ADMIN / LIBRARIAN: Search students
    @GetMapping("/search")
    public List<Student> searchStudents(
            @RequestParam String keyword) {

        return studentRepository
                .findByNameContainingIgnoreCase(keyword);
    }
}
