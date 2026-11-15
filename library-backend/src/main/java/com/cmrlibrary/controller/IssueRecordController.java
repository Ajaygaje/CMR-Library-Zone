
package com.cmrlibrary.controller;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cmrlibrary.entity.Book;
import com.cmrlibrary.entity.IssueRecord;
import com.cmrlibrary.entity.Student;
import com.cmrlibrary.repository.BookRepository;
import com.cmrlibrary.repository.IssueRecordRepository;
import com.cmrlibrary.repository.StudentRepository;

@RestController
@RequestMapping("/api/issues")
@CrossOrigin(origins = "*")
public class IssueRecordController {

    private final IssueRecordRepository issueRepository;
    private final BookRepository bookRepository;
    private final StudentRepository studentRepository;

    private static final int LOAN_DAYS = 14;
    private static final double FINE_PER_DAY = 5.0;

    public IssueRecordController(
            IssueRecordRepository issueRepository,
            BookRepository bookRepository,
            StudentRepository studentRepository) {

        this.issueRepository = issueRepository;
        this.bookRepository = bookRepository;
        this.studentRepository = studentRepository;
    }

    // Check whether the authenticated user is a STUDENT
    private boolean isStudent(Authentication authentication) {

        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_STUDENT"));
    }

    // ADMIN / LIBRARIAN: View all issue records
    @GetMapping
    public List<IssueRecord> getAllIssues() {

        return issueRepository.findAll();
    }

    // STUDENT / ADMIN / LIBRARIAN: Issue a book
    @PostMapping("/issue")
    @Transactional
    public ResponseEntity<?> issueBook(
            @RequestBody IssueRecord issueRecord,
            Authentication authentication) {

        // Validate book ID
        if (issueRecord.getBookId() == null) {

            return ResponseEntity.badRequest()
                    .body("Book ID is required");
        }

        // Find book
        Book book = bookRepository.findById(issueRecord.getBookId())
                .orElse(null);

        if (book == null) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Book not found");
        }

        Student student;

        // STUDENT can borrow only for their own account
        if (isStudent(authentication)) {

            student = studentRepository
                    .findByEmail(authentication.getName());

            if (student == null) {

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Student profile not found for this account");
            }

        } else {

            // ADMIN / LIBRARIAN can issue to any student
            if (issueRecord.getStudentId() == null) {

                return ResponseEntity.badRequest()
                        .body("Student ID is required");
            }

            student = studentRepository
                    .findById(issueRecord.getStudentId())
                    .orElse(null);

            if (student == null) {

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Student not found");
            }
        }

        // Set the actual student's ID
        issueRecord.setStudentId(student.getId());

        // Prevent duplicate active borrowing
        boolean alreadyIssued =
                issueRepository.existsByStudentIdAndBookIdAndStatus(
                        student.getId(),
                        book.getId(),
                        "ISSUED"
                );

        if (alreadyIssued) {

            return ResponseEntity.badRequest()
                    .body("You have already borrowed this book. Please return it before borrowing again.");
        }

        // Check book availability
        if (book.getAvailableQuantity() <= 0) {

            return ResponseEntity.badRequest()
                    .body("Book is not available");
        }

        // Reduce available quantity
        book.setAvailableQuantity(
                book.getAvailableQuantity() - 1);

        bookRepository.save(book);

        // Set issue date and due date
        LocalDate today = LocalDate.now();

        issueRecord.setIssueDate(today);
        issueRecord.setDueDate(today.plusDays(LOAN_DAYS));

        issueRecord.setReturnDate(null);
        issueRecord.setStatus("ISSUED");
        issueRecord.setFine(0.0);

        // Save issue record
        IssueRecord savedRecord =
                issueRepository.save(issueRecord);

        return ResponseEntity.ok(savedRecord);
    }

    // ADMIN / LIBRARIAN: Return a book and calculate fine
    @PutMapping("/return/{id}")
    @Transactional
    public ResponseEntity<?> returnBook(
            @PathVariable Long id) {

        IssueRecord record = issueRepository.findById(id)
                .orElse(null);

        if (record == null) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Issue record not found");
        }

        if ("RETURNED".equalsIgnoreCase(record.getStatus())) {

            return ResponseEntity.badRequest()
                    .body("Book has already been returned");
        }

        LocalDate today = LocalDate.now();

        record.setReturnDate(today);
        record.setStatus("RETURNED");

        // Calculate overdue days
        long overdueDays = ChronoUnit.DAYS.between(
                record.getDueDate(), today);

        double fine = 0.0;

        if (overdueDays > 0) {

            fine = overdueDays * FINE_PER_DAY;
        }

        record.setFine(fine);

        // Increase available book quantity
        Book book = bookRepository.findById(record.getBookId())
                .orElse(null);

        if (book != null) {

            book.setAvailableQuantity(
                    book.getAvailableQuantity() + 1);

            bookRepository.save(book);
        }

        // Save returned issue record
        IssueRecord savedRecord =
                issueRepository.save(record);

        return ResponseEntity.ok(savedRecord);
    }

    // ADMIN / LIBRARIAN: View issue records of a student
    @GetMapping("/student/{studentId}")
    public List<IssueRecord> getStudentIssues(
            @PathVariable Long studentId) {

        return issueRepository.findByStudentId(studentId);
    }

    // ADMIN / LIBRARIAN: View all active issues
    @GetMapping("/active")
    public List<IssueRecord> getActiveIssues() {

        return issueRepository.findByStatus("ISSUED");
    }

    // STUDENT: View own borrowing history
    @GetMapping("/my-history")
    public ResponseEntity<?> getMyHistory(
            Authentication authentication) {

        Student student = studentRepository
                .findByEmail(authentication.getName());

        if (student == null) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Student profile not found");
        }

        return ResponseEntity.ok(
                issueRepository.findByStudentId(student.getId()));
    }

    // STUDENT: View own active borrowed books
    @GetMapping("/my-active")
    public ResponseEntity<?> getMyActiveBooks(
            Authentication authentication) {

        Student student = studentRepository
                .findByEmail(authentication.getName());

        if (student == null) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Student profile not found");
        }

        return ResponseEntity.ok(
                issueRepository.findByStudentIdAndStatus(
                        student.getId(), "ISSUED"));
    }
}
