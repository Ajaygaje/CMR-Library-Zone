
package com.cmrlibrary.controller;

import java.util.List;

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

import com.cmrlibrary.entity.Book;
import com.cmrlibrary.repository.BookRepository;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookController {

    private final BookRepository bookRepository;

    // Constructor
    public BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // 1. GET - Get all books
    @GetMapping
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // 2. POST - Add a new book
    @PostMapping
    public Book addBook(@RequestBody Book book) {
        return bookRepository.save(book);
    }

    // 3. PUT - Update an existing book
    @PutMapping("/{id}")
    public Book updateBook(
            @PathVariable Long id,
            @RequestBody Book updatedBook) {

        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        existingBook.setTitle(updatedBook.getTitle());
        existingBook.setAuthor(updatedBook.getAuthor());
        existingBook.setIsbn(updatedBook.getIsbn());
        existingBook.setDepartment(updatedBook.getDepartment());
        existingBook.setQuantity(updatedBook.getQuantity());
        existingBook.setAvailableQuantity(updatedBook.getAvailableQuantity());

        return bookRepository.save(existingBook);
    }

    // 4. DELETE - Delete a book
    @DeleteMapping("/{id}")
    public String deleteBook(@PathVariable Long id) {

        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        bookRepository.delete(existingBook);

        return "Book deleted successfully!";
    }

    // 5. SEARCH - Search books by title, author or ISBN
    @GetMapping("/search")
    public List<Book> searchBooks(@RequestParam String keyword) {

        return bookRepository
                .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase(
                        keyword,
                        keyword,
                        keyword
                );
    }
}
