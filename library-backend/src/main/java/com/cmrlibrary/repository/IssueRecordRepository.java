package com.cmrlibrary.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cmrlibrary.entity.IssueRecord;

public interface IssueRecordRepository extends JpaRepository<IssueRecord, Long> {

    // Find all issue records of a student
    List<IssueRecord> findByStudentId(Long studentId);

    // Find all issue records of a book
    List<IssueRecord> findByBookId(Long bookId);

    // Find records by status
    List<IssueRecord> findByStatus(String status);

    // Find currently issued books of a student
    List<IssueRecord> findByStudentIdAndStatus(Long studentId, String status);

    // Check whether a student already has this book issued
    boolean existsByStudentIdAndBookIdAndStatus(
            Long studentId,
            Long bookId,
            String status
    );
}