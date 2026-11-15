# CMR LIBRARY ZONE
## Smart Library Management System

### 1. Project Overview

CMR Library Zone is a web-based Smart Library Management System developed to digitize and simplify the daily activities of a college library.

The application provides a centralized platform where administrators, librarians, and students can access different functionalities according to their roles.

The main objective is to reduce manual library operations, maintain accurate book records, manage student information, simplify book issuing and returning, and provide an organized digital library experience.

The application uses a modern frontend, Java Spring Boot backend, and MySQL database.

### 2. Project Objectives

- Digitize traditional library management activities.
- Maintain book and student records efficiently.
- Provide secure login for different user roles.
- Simplify book issuing and returning.
- Track issued books, due dates, and overdue fines.
- Allow students to search and borrow available books.
- Provide an easy-to-use and responsive interface.
- Maintain accurate and centralized database records.

### 3. Technology Stack

**Frontend**
- HTML5
- CSS3
- JavaScript
- Font Awesome
- Responsive web design

**Backend**
- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- JWT Authentication
- BCrypt Password Encryption

**Database**
- MySQL 8.0
- MySQL Workbench

**Development Tools**
- Visual Studio Code
- Apache Maven
- Git
- GitHub
- VS Code Live Server

### 4. User Roles and Functionalities

#### A. Admin Module

The Admin has access to the main management functionalities.

Features:

1. Admin Login
   - Secure administrator authentication.
   - Role-based access control.

2. Dashboard
   - Total number of books.
   - Total registered students.
   - Currently issued books.
   - Returned books.
   - Overdue books.

3. Book Management
   - Add new books.
   - View all books.
   - Edit book information.
   - Delete books.
   - Manage book quantities and availability.

4. Student Management
   - View registered students.
   - Search students.
   - View student information.
   - Delete student records.

5. Librarian Management
   - Create librarian accounts.
   - View librarian accounts.
   - Manage librarian accounts.

#### B. Librarian Module

The Librarian manages daily library activities.

Features:

1. Secure Librarian Login.
2. Librarian Dashboard.
3. View available books.
4. Add, edit, and manage books.
5. View registered students.
6. Search student records.
7. Issue books to students.
8. Return issued books.
9. View active issue records.
10. View overdue books.
11. Calculate overdue fines.
12. View borrowing records.

#### C. Student Module

Students can access library services using their registered accounts.

Features:

1. Student Registration
   - Register using personal and academic details.
   - Secure password storage.

2. Student Login
   - Authenticate using registered credentials.

3. Student Dashboard
   - View available books.
   - Search books.
   - Filter books by department.
   - View book availability.

4. Book Borrowing
   - Borrow available books.
   - Prevent duplicate borrowing of the same book while it is already issued.
   - Display borrowing information.

5. My Issued Books
   - View currently borrowed books.
   - Check issue dates.
   - Check due dates.

6. Borrowing History
   - View previously borrowed books.
   - View returned book records.
   - View applicable fines.

7. Student Profile
   - View student details.
   - Update phone number.

### 5. Book Issue and Return Workflow

The system follows a simple book borrowing process.

**Book Issue Process**

1. Student logs into the application.
2. Student browses available books.
3. Student selects a book.
4. System checks book availability.
5. System checks whether the student already has the same book issued.
6. If available and eligible, the system creates an issue record.
7. Available book quantity decreases by one.
8. The issue date and due date are automatically generated.

**Book Return Process**

1. Librarian accesses the issue records.
2. Librarian selects the book to return.
3. System updates the return date.
4. System changes the issue status to RETURNED.
5. System calculates any overdue fine.
6. Available book quantity increases by one.

### 6. Library Rules

- Standard borrowing period: 14 days.
- Overdue fine: ₹5 per day.
- Students cannot borrow the same book again while they have an active issue for it.
- Students can borrow the same book again after returning it, subject to availability.
- Only authorized administrators and librarians can manage library records.
- Students can view their own borrowing history and profile.

### 7. Backend Architecture

The backend follows a layered Spring Boot architecture.

**Controller Layer**
- BookController
- StudentController
- UserController
- IssueRecordController
- DashboardController
- HealthController

**Entity Layer**
- Book
- Student
- User
- IssueRecord

**Repository Layer**
- BookRepository
- StudentRepository
- UserRepository
- IssueRecordRepository

**Configuration Layer**
- SecurityConfig
- JwtService
- JwtAuthenticationFilter
- PasswordConfig

The backend exposes REST APIs that connect the frontend with the MySQL database.

### 8. Authentication and Security

The application uses Spring Security and JWT authentication.

Security features include:

- Login authentication.
- JWT token generation and validation.
- BCrypt password hashing.
- Role-based authorization.
- Protected API endpoints.
- Student-specific profile and borrowing access.
- Students cannot issue books for other students.
- Administrators and librarians have management permissions according to their roles.

### 9. Database Design

The application uses MySQL as its relational database.

Main database tables:

**Users**
- id
- name
- email
- password
- role

**Students**
- id
- name
- roll_number
- email
- department
- phone

**Books**
- id
- book details
- department
- quantity
- available_quantity

**Issue Records**
- id
- student_id
- book_id
- issue_date
- due_date
- return_date
- status
- fine

The entities are managed using Spring Data JPA.

### 10. Frontend Pages

The public-facing website includes:

- Home Page
- Gallery Page
- About Page
- Admin Login Page
- Librarian Login Page
- Student Login Page
- Student Registration Page

The authenticated application includes:

- Admin Dashboard
- Librarian Dashboard
- Student Dashboard

The website uses a blue-and-white professional theme with responsive layouts.

### 11. Project Structure

CMR Library Zone/

├── frontend/
│   ├── index.html
│   ├── about.html
│   ├── gallery.html
│   ├── adminlogin.html
│   ├── librarianlogin.html
│   ├── studentlogin.html
│   ├── studentregister.html
│   ├── admin_panel.html
│   ├── librarian_panel.html
│   ├── departments.html
│   └── images and frontend assets
│
├── library-backend/
│   ├── src/main/java/com/cmrlibrary/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── entity/
│   │   └── repository/
│   ├── src/main/resources/
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
│
└── README.md

### 12. API Functionality

The backend provides REST endpoints for:

- User registration and login.
- Book CRUD operations.
- Student CRUD operations.
- Student search and profile management.
- Book issuing and returning.
- Student borrowing history.
- Active issue records.
- Dashboard statistics.

The frontend communicates with these APIs using JavaScript Fetch requests.

### 13. Main Advantages

- Reduces manual paperwork.
- Improves library record management.
- Provides centralized data storage.
- Makes book searching easier.
- Improves transparency in borrowing and returning.
- Helps librarians track overdue books.
- Provides role-based access.
- Saves time for students and library staff.

### 14. Future Scope

Possible improvements, only when required:

- Book cover image management.
- Exporting library reports.
- Email notifications for due dates.
- Deployment to a cloud server.

These are optional future improvements and are not required for the current project.

### 15. Conclusion

CMR Library Zone is a practical Smart Library Management System designed to modernize college library operations.

By combining HTML, CSS, JavaScript, Java Spring Boot, Spring Security, JWT, and MySQL, the system provides an organized platform for managing books, students, librarians, and borrowing activities.

The project demonstrates practical knowledge of full-stack web development, REST API integration, relational database management, authentication, authorization, and CRUD operations.

The primary goal is to provide a simple, secure, reliable, and user-friendly library management experience.
