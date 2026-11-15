
package com.cmrlibrary.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cmrlibrary.entity.Book;
import com.cmrlibrary.entity.IssueRecord;
import com.cmrlibrary.repository.BookRepository;
import com.cmrlibrary.repository.IssueRecordRepository;
import com.cmrlibrary.repository.StudentRepository;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final BookRepository bookRepository;
    private final StudentRepository studentRepository;
    private final IssueRecordRepository issueRepository;

    public DashboardController(
            BookRepository bookRepository,
            StudentRepository studentRepository,
            IssueRecordRepository issueRepository) {

        this.bookRepository = bookRepository;
        this.studentRepository = studentRepository;
        this.issueRepository = issueRepository;
    }

    @GetMapping("/stats")
    public Map<String, Object> getDashboardStats() {

        Map<String, Object> stats = new HashMap<>();

        List<Book> books = bookRepository.findAll();
        List<IssueRecord> issues = issueRepository.findAll();

        int totalBooks = books.stream()
                .mapToInt(Book::getQuantity)
                .sum();

        long totalStudents = studentRepository.count();

        long issuedBooks = issues.stream()
                .filter(issue -> "ISSUED".equals(issue.getStatus()))
                .count();

        long returnedBooks = issues.stream()
                .filter(issue -> "RETURNED".equals(issue.getStatus()))
                .count();

        long overdueBooks = issues.stream()
                .filter(issue ->
                        "ISSUED".equals(issue.getStatus())
                        && issue.getDueDate().isBefore(LocalDate.now()))
                .count();

        stats.put("totalBooks", totalBooks);
        stats.put("totalStudents", totalStudents);
        stats.put("issuedBooks", issuedBooks);
        stats.put("returnedBooks", returnedBooks);
        stats.put("overdueBooks", overdueBooks);

        return stats;
    }
}
