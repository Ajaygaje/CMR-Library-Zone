
package com.cmrlibrary.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cmrlibrary.config.JwtService;
import com.cmrlibrary.entity.Student;
import com.cmrlibrary.entity.User;
import com.cmrlibrary.repository.StudentRepository;
import com.cmrlibrary.repository.UserRepository;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserController(
            UserRepository userRepository,
            StudentRepository studentRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // Check Admin permission
    private boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // PUBLIC REGISTRATION: STUDENT ONLY
    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> registerUser(
            @RequestBody StudentRegistrationRequest request) {

        if (request.getName() == null || request.getName().isBlank()
                || request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()
                || request.getRollNumber() == null || request.getRollNumber().isBlank()
                || request.getDepartment() == null || request.getDepartment().isBlank()
                || request.getPhone() == null || request.getPhone().isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of("message", "All fields are required"));
        }

        String name = request.getName().trim();
        String email = request.getEmail().trim().toLowerCase();
        String rollNumber = request.getRollNumber().trim();

        if (userRepository.existsByEmail(email)
                || studentRepository.findByEmail(email) != null) {

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Email already registered"));
        }

        if (studentRepository.findByRollNumber(rollNumber) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Roll number already registered"));
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("STUDENT");

        userRepository.save(user);

        Student student = new Student();
        student.setName(name);
        student.setEmail(email);
        student.setRollNumber(rollNumber);
        student.setDepartment(request.getDepartment().trim());
        student.setPhone(request.getPhone().trim());

        Student savedStudent = studentRepository.save(student);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "message", "Student registered successfully",
                        "studentId", savedStudent.getId(),
                        "name", savedStudent.getName(),
                        "email", savedStudent.getEmail(),
                        "rollNumber", savedStudent.getRollNumber(),
                        "role", "STUDENT"
                )
        );
    }

    // ADMIN-ONLY ACCOUNT CREATION
    @PostMapping("/create")
    public ResponseEntity<?> createUser(
            @RequestBody User user,
            Authentication authentication) {

        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Only Admin can create accounts"));
        }

        if (user.getName() == null || user.getName().isBlank()
                || user.getEmail() == null || user.getEmail().isBlank()
                || user.getPassword() == null || user.getPassword().isBlank()
                || user.getRole() == null || user.getRole().isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of("message", "All fields are required"));
        }

        String role = user.getRole().trim().toUpperCase();

        if (!Set.of("LIBRARIAN", "STUDENT").contains(role)) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Admin can create only LIBRARIAN or STUDENT accounts"
                    ));
        }

        String email = user.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Email already registered"));
        }

        user.setEmail(email);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "message", "Account created successfully",
                        "id", savedUser.getId(),
                        "name", savedUser.getName(),
                        "email", savedUser.getEmail(),
                        "role", savedUser.getRole()
                )
        );
    }

    // ADMIN-ONLY: VIEW ALL LIBRARIANS
    @GetMapping("/librarians")
    public ResponseEntity<?> getLibrarians(
            Authentication authentication) {

        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Only Admin can view librarians"));
        }

        List<Map<String, Object>> librarians = userRepository.findAll()
                .stream()
                .filter(user -> "LIBRARIAN".equalsIgnoreCase(user.getRole()))
                .map(user -> Map.<String, Object>of(
                        "id", user.getId(),
                        "name", user.getName(),
                        "email", user.getEmail(),
                        "role", user.getRole()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(librarians);
    }

    // ADMIN-ONLY: DELETE A LIBRARIAN
    @DeleteMapping("/librarians/{id}")
    public ResponseEntity<?> deleteLibrarian(
            @PathVariable Long id,
            Authentication authentication) {

        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Only Admin can delete librarians"));
        }

        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        if (!"LIBRARIAN".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "This account is not a librarian"));
        }

        userRepository.delete(user);

        return ResponseEntity.ok(
                Map.of("message", "Librarian deleted successfully")
        );
    }

    // USER LOGIN WITH JWT TOKEN
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(
            @RequestBody Map<String, String> loginData) {

        String email = loginData.get("email");
        String password = loginData.get("password");

        if (email == null || password == null
                || email.isBlank() || password.isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email and password are required"));
        }

        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElse(null);

        if (user == null
                || !passwordEncoder.matches(password, user.getPassword())) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password"));
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message", "Login successful",
                        "id", user.getId(),
                        "name", user.getName(),
                        "email", user.getEmail(),
                        "role", user.getRole(),
                        "token", token
                )
        );
    }

    // REGISTRATION REQUEST DATA
    public static class StudentRegistrationRequest {

        private String name;
        private String email;
        private String password;
        private String rollNumber;
        private String department;
        private String phone;

        public StudentRegistrationRequest() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getRollNumber() {
            return rollNumber;
        }

        public void setRollNumber(String rollNumber) {
            this.rollNumber = rollNumber;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
}
