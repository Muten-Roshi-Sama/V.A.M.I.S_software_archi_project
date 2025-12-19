# V.A.M.I.S (Virtual Academic Management and Information System)

## Overview
V.A.M.I.S is a full-stack, multi-role academic management system built with Kotlin, Ktor, Exposed ORM, and Compose Multiplatform. It supports robust CRUD operations, authentication, and role-based dashboards for Admins, Students, and Teachers. The backend is designed for extensibility, testability, and clean separation of concerns.

## Features
- **Test-driven development:** Tests are run when building the app, ensuring everything works as expected.
- **Role-Based Authentication:** JWT-secured login for Admin, Student, and Teacher accounts.
- **Protected Endpoints:** All API endpoints are user-protected.
- **Admin Panel:** Full CRUD for users, role management, and system monitoring.
- **Student & Teacher Dashboards:** Secure self-service endpoints for profile management and password updates.
- **Course & Grade Management:** Endpoints for managing courses, grades, schedules, and bulletins.
- **Seeded Database:** Automatic test/seed data loading for reliable development and CI.
- **Comprehensive Test Suite:** End-to-end route tests for all major roles and services.

## Tech Stack
- **Backend:** Kotlin, Ktor, Exposed ORM, SQLite
- **Frontend:** Compose Multiplatform (Android/iOS/Desktop/Web)
- **Testing:** Ktor TestApplication, JUnit, Kotlin Test
- **Build:** Gradle (Kotlin DSL)

## Backend Structure
```
server/
src/
main/
kotlin/be/ecam/server/
Application.kt # Ktor entrypoint
models/ # Exposed ORM models (Admin, Student, Teacher, etc.)
services/ # Business logic (AdminService, StudentService, etc.)
routes/ # Ktor route handlers (AdminRoutes, StudentRoutes, etc.)
db/ # DatabaseFactory, SeedManager, migrations
auth/ # JWT, authentication helpers
util/ # Validation, helpers
resources/
data/ # Seed JSONs for admins, students, teachers, etc.
test/
kotlin/be/ecam/server/
routes/ # Route-level tests (AdminRoutesTest, StudentRoutesTest, TeacherRoutesTest)
services/ # Service-level tests
models/ # Model CRUD tests
db/ # Seed validation tests
testutils/ # Test DB setup
util/ # Utility tests
```



## Getting Started

### Prerequisites
- JDK 17+
- Gradle (wrapper included)
- (Optional) Android Studio or IntelliJ IDEA for Compose frontend

### Running the Backend
```bash
cd server

./gradlew :server:run
```

Other useful commands:

> :server:build

> :server:clean

The server will start on http://localhost:8080 by default.

### Running Tests
All tests use seeded test accounts.

Default test credentials (see `LoginHelper.kt`):

- Admin: `TESTADMIN@admin.com` / `pass123`
- Student: `TESTSTUDENT@student.com` / `pass123`
- Teacher: `TESTTEACHER@teacher.com` / `pass123`


## API Overview
- `/auth/login:` Authenticate and receive JWT token.
- `/crud/admins:` Admin CRUD endpoints (list, create, update, delete, self-profile).
- `/crud/students:` Student CRUD endpoints (admin and self-service).
- `/crud/teachers:` Teacher CRUD endpoints (admin and self-service).
- `/crud/schedules, /crud/bulletins, /crud/courses, etc.:` Additional endpoints for academic data.
All endpoints are protected by role-based guards. See route files for details.



## Dev Notes
- Database Seeding: On first run or test, the database is seeded from data.
- Test Isolation: Each test class initializes a fresh DB state for reliability.
- Extensibility: Add new roles/services/routes by following the existing pattern in services/ and routes/handlers/.



## Next to implement
### Frontend
- Hide features (grey out) based on roles.​
- Solve out some UI features/bugs​
- Link PAE window with backend
### Backend
- Test files for AcademicTables routes, models, and services.
- Protection of AcademicTables Routes using predefined role-guards.
- Seeding of AcademicTables using the actual main SeedManager.kt
- Verify SOLID principles for AcademicTables




## Authors and Contributors

| Name   | GitHub                                      | Role/Area | Main Contributions |
|--------|---------------------------------------------|-----------|--------------------|
| Ayoub  | [Ayoub21061](https://github.com/Ayoub21061) | Frontend  | - Full frontend integration for iOS<br>- Full frontend integration for Android<br>- User calendar UI and features |
| Ibtihal| [Ibtihal-mrn](https://github.com/Ibtihal-mrn)| Frontend  | - User dashboards<br>- Login screen<br>- App navigation UI<br>- Teacher and admin UI |
| Mehdi  | [Melmaado](https://github.com/Melmaado)      | Frontend  | - UI mockups<br>- Dashboard design<br>- Student UI components Grades, Annual Study Program |
| Salwa  | [SalwaHm](https://github.com/SalwaHm)        | Backend   | - Academic tables (courses, schedules, options, etc.)<br>- Backend model/service/route setup<br>- Frontend integration of academic tables |
| Vass   | [Muten-Roshi-Sama](https://github.com/Muten-Roshi-Sama) | Backend | - Backend foundations<br>- User models/services/utils/CRUD Routes and tests<br>- DTO/DAO models serialization for frontend ease-of-use<br>- JWT authentication and session management<br>- Database seeding/setup<br>- Security and endpoints are protected by role-based guards<br>- Automated test integration of users CRUD endpoints/authentication<br>- Fullstack/backend-frontend connections (KtorApiRepository) |


