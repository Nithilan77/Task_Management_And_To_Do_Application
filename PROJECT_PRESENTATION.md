# Project Presentation: Task Management & To-Do Application

This document is intended as a full presentation for an academic or technical review. It explains the project from first principles, details how Java and the DBMS are integrated, walks through every important class, method and data model, explains normalization and schema design, and points to exact code locations for review.

---

## Contents

1. Executive summary
2. Problem statement & goals
3. Functional & non-functional requirements
4. High-level architecture
5. How Java is used (packages, classes, objects, major methods)
6. Persistence and DBMS integration (Hibernate, configuration, SQL)
7. Data model and normalization
8. Key code walkthrough with file references and snippets
9. How the UI works (JavaFX, controllers, FXML)
10. Security and production notes
11. How to run, test, and demonstrate
12. Appendix: relevant files and commands

---

## 1. Executive summary

A lightweight desktop Task Management & To‑Do application implemented in Java using JavaFX for the UI and Hibernate (JPA) for persistence. The system allows users to register and login, create/edit/delete tasks, mark tasks completed, filter and search tasks, and store per-user preferences.

This document explains design decisions, class structure, database integration, normalization, and points you to the exact files in the repository so you can inspect or run the code during a presentation.

---

## 2. Problem statement & goals

Problem: Provide a simple, reliable task management tool for users to store and manage to‑dos locally with a clean UI, persistent storage, and sensible filtering/sorting options.

Goals:
- Implement user accounts and per-user task storage.
- Provide CRUD operations for tasks with priority, deadline, and completion status.
- Use a relational DB (Oracle) for durability and to demonstrate DBMS integration.
- Provide a clear architecture separating UI, business logic, and persistence.

---

## 3. Functional & non-functional requirements

Functional:
- Users can register and login.
- Users can create, edit, delete tasks.
- Tasks have: title, description, priority, deadline, created/updated timestamps, owner.
- Users can filter tasks by priority/status and search by text.
- Preferences are stored per-user (key/value pairs).

Non-functional:
- Persistent storage (reliable DB operations with transactions).
- Responsive, intuitive UI with JavaFX.
- Separation of concerns: controllers (UI) -> service (business) -> persistence (Hibernate).
- Secure defaults: avoid auto DDL in production, enforce unique emails, avoid storing plaintext credentials in repo.

---

## 4. High-level architecture

- Presentation: JavaFX (FXML) views and Controllers (LoginController, DashboardController, TaskController).
- Application: `MainApp` bootstraps configuration and routing.
- Business logic: `DatabaseService` singleton handles transactional operations.
- Persistence: Hibernate (configured via `hibernate.cfg.xml`), entity classes `User`, `Task`, and `UserPreference`.
- Configuration: `ConfigManager` reads `application.properties` and environment variables.

File locations (most important):
- `src/main/java/com/taskmanager/MainApp.java` (application entry)
- `src/main/java/com/taskmanager/config/ConfigManager.java` (config)
- `src/main/java/com/taskmanager/util/HibernateUtil.java` (session factory)
- `src/main/java/com/taskmanager/service/DatabaseService.java` (business & persistence operations)
- `src/main/java/com/taskmanager/entity/` (entities)
- `src/main/java/com/taskmanager/controller/` (controllers and SceneRouter)
- `src/main/resources/hibernate.cfg.xml` (Hibernate configuration)
- `src/main/resources/sql/create_schema_oracle.sql` (manual DDL)

---

## 5. How Java is used: packages, classes, objects, and methods

We used Java to implement the full application stack. Below is a package-by-package map and the important classes, objects and methods. For each class you'll find: purpose, important methods, key interactions, and file path.

### 5.1 Entry point

- Main class: `com.taskmanager.MainApp`
  - File: `src/main/java/com/taskmanager/MainApp.java`
  - Purpose: JavaFX `Application` subclass. Boots the `ConfigManager`, `DatabaseService` and `SceneRouter`.
  - Important methods:
    - `public void start(Stage primaryStage)` — initializes configuration, database service, scene router and shows the login screen.
    - `public void stop()` — calls `DatabaseService.getInstance().close()` to shutdown Hibernate.
    - `public static void main(String[] args)` — calls `launch(args)` to start JavaFX.

Snippet (startup sequence — small excerpt)

```java
// File: src/main/java/com/taskmanager/MainApp.java
@Override
public void start(Stage primaryStage) {
    ConfigManager configManager = ConfigManager.getInstance();
    DatabaseService databaseService = DatabaseService.getInstance();
    SceneRouter.getInstance().init(primaryStage);
}
```

### 5.2 Configuration

- `com.taskmanager.config.ConfigManager`
  - File: `src/main/java/com/taskmanager/config/ConfigManager.java`
  - Purpose: Loads application properties from `application.properties`, falls back to environment vars and system properties.
  - Important methods (examples):
    - `getDbUrl()`, `getDbUsername()`, `getDbPassword()` — database connection values.
    - `getHibernateHbm2ddl()` — returns configured `hibernate.hbm2ddl.auto` (default `update`).
    - `printConfigStatus()` — useful for debugging during startup.

### 5.3 Persistence utility

- `com.taskmanager.util.HibernateUtil`
  - File: `src/main/java/com/taskmanager/util/HibernateUtil.java`
  - Purpose: Controls a singleton Hibernate `SessionFactory` using `hibernate.cfg.xml`.
  - Important methods:
    - `public static SessionFactory getSessionFactory()` — lazily builds and returns the `SessionFactory`.
    - `public static void shutdown()` — closes the `SessionFactory`.

Snippet (HibernateUtil.getSessionFactory)

```java
// File: src/main/java/com/taskmanager/util/HibernateUtil.java
public static SessionFactory getSessionFactory() {
    if (sessionFactory == null) {
        sessionFactory = new Configuration()
                .configure("hibernate.cfg.xml")
                .buildSessionFactory();
    }
    return sessionFactory;
}
```

### 5.4 Business & Data Access

- `com.taskmanager.service.DatabaseService` (Singleton)
  - File: `src/main/java/com/taskmanager/service/DatabaseService.java`
  - Purpose: Centralized database operations using Hibernate Sessions/Transactions. All CRUD is here.
  - Pattern: Singleton with `getInstance()`.
  - Key methods for Users:
    - `public User authenticateUser(String email, String password)` — queries for a matching user and updates `lastLogin`.
    - `public User registerUser(String email, String password, String displayName)` — checks for existing email and persists a new `User`.
    - `public User getUserById(int userId)` — fetch by primary key.
  - Key methods for Tasks:
    - `public Task saveTask(Task task)` — `session.merge(task)` in a transaction.
    - `public List<Task> getUserTasks(int userId)` — HQL: `FROM Task WHERE user.id = :userId ORDER BY createdAt DESC`.
    - `public List<Task> getUserTasksByStatus(int userId, boolean completed)`
    - `public List<Task> getUserTasksByPriority(int userId, String priority)`
    - `public void deleteTask(int taskId)` — removes Task if present.
    - `public Task updateTask(Task task)` — merges and sets updated timestamp.
  - Preferences:
    - `saveUserPreference`, `getUserPreference`, `getUserPreferences`.
  - Transaction handling: opens session, begins transaction, try/catch with rollback on exception.

Snippet (authentication and task save examples)

```java
// File: src/main/java/com/taskmanager/service/DatabaseService.java
public User authenticateUser(String email, String password) {
  try (Session session = HibernateUtil.getSessionFactory().openSession()) {
    Transaction tx = session.beginTransaction();
    Query<User> query = session.createQuery(
      "FROM User WHERE email = :email AND password = :password", User.class);
    query.setParameter("email", email);
    query.setParameter("password", password);
    User user = query.uniqueResult();
    if (user != null) { user.setLastLogin(LocalDateTime.now()); session.merge(user); }
    tx.commit();
    return user;
  }
}

public Task saveTask(Task task) {
  try (Session session = HibernateUtil.getSessionFactory().openSession()) {
    Transaction tx = session.beginTransaction();
    session.merge(task);
    tx.commit();
    return task;
  }
}
```

### 5.5 Entities (JPA)

All entities live under `src/main/java/com/taskmanager/entity/` and are annotated with Jakarta Persistence annotations.

- `com.taskmanager.entity.User` — `User.java`
  - Fields: `id` (sequence USER_SEQ), `email` (unique), `password`, `displayName`, `createdAt`, `lastLogin`, `tasks` (OneToMany), `preferences` (OneToMany).
  - Constructors: default sets `createdAt`, convenience constructor `User(String email, String password, String displayName)`.
  - Methods: getters/setters, `addTask(Task)`, `removeTask(Task)`.

- `com.taskmanager.entity.Task` — `Task.java`
  - Fields: `id` (sequence TASK_SEQ), `title` (not null), `description`, `priority`, `deadline`, `completed` (boolean), `createdAt`, `updatedAt`, `user` (ManyToOne).
  - Lifecycle: `@PreUpdate preUpdate()` updates `updatedAt`.
  - Methods: getters/setters; setting `completed` updates `updatedAt`.

  Snippet (Task entity core fields & annotations)

  ```java
  // File: src/main/java/com/taskmanager/entity/Task.java
  @Entity
  @Table(name = "tasks")
  public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_seq")
    @SequenceGenerator(name = "task_seq", sequenceName = "TASK_SEQ", allocationSize = 1)
    private int id;

    @Column(nullable = false)
    private String title;

    private String description;
    private String priority; // High, Medium, Low
    private LocalDate deadline;
    private boolean completed;
    // createdAt, updatedAt, user relation omitted for brevity
  }
  ```

- `com.taskmanager.entity.UserPreference` — `UserPreference.java`
  - Fields: `id` (sequence PREFERENCE_SEQ), `key`, `value`, `createdAt`, `user` (ManyToOne).

File paths:
- `src/main/java/com/taskmanager/entity/User.java`
- `src/main/java/com/taskmanager/entity/Task.java`
- `src/main/java/com/taskmanager/entity/UserPreference.java`

---

## 6. Persistence and DBMS integration

The application uses Hibernate (JPA) as the ORM and is configured with `hibernate.cfg.xml` to connect to an Oracle database.

File: `src/main/resources/hibernate.cfg.xml`
- Key properties used:
  - `hibernate.connection.driver_class` — `oracle.jdbc.driver.OracleDriver`
  - `hibernate.connection.url` — `jdbc:oracle:thin:@localhost:1521:xe`
  - `hibernate.connection.username` & `hibernate.connection.password` — currently set to `system` / `Meenakshi@10` (replace for production)
  - `hibernate.dialect` — `org.hibernate.community.dialect.Oracle12cDialect`
  - `hibernate.hbm2ddl.auto` — `update` (development convenience). See security notes for production changes.

  Snippet (`hibernate.cfg.xml` — connection/DDL mode)

  ```xml
  <!-- File: src/main/resources/hibernate.cfg.xml -->
  <property name="hibernate.connection.url">jdbc:oracle:thin:@localhost:1521:xe</property>
  <property name="hibernate.connection.username">system</property>
  <property name="hibernate.connection.password">Meenakshi@10</property>
  <property name="hibernate.dialect">org.hibernate.community.dialect.Oracle12cDialect</property>
  <property name="hibernate.hbm2ddl.auto">update</property>
  ```

How integration is wired:
- `HibernateUtil.getSessionFactory()` reads `hibernate.cfg.xml` (line: `new Configuration().configure("hibernate.cfg.xml").buildSessionFactory()`) and builds a `SessionFactory`.
- `DatabaseService` uses `HibernateUtil.getSessionFactory().openSession()` to open a `Session`, starts a `Transaction`, executes queries or `persist/merge/remove`, then commits or rolls back.
- Entities are mapped by `@Entity` and listed in `hibernate.cfg.xml` mapping entries.

Manual SQL provided:
- `src/main/resources/sql/create_schema_oracle.sql` — complete DDL with sequences (`TASK_SEQ`, `USER_SEQ`, `PREFERENCE_SEQ`), tables (`users`, `tasks`, `user_preferences`), FK constraints and optional triggers.

Snippet (DDL excerpt — sequences & users table)

```sql
-- File: src/main/resources/sql/create_schema_oracle.sql
CREATE SEQUENCE TASK_SEQ START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE USER_SEQ START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE TABLE users (
  id NUMBER(10) PRIMARY KEY,
  email VARCHAR2(255) NOT NULL,
  password VARCHAR2(255) NOT NULL,
  display_name VARCHAR2(255),
  created_at TIMESTAMP,
  last_login TIMESTAMP
);
ALTER TABLE users ADD CONSTRAINT uq_users_email UNIQUE (email);
```

Why manual DDL?
- For controlled production deployments, manual DDL minimizes accidental schema changes. `hibernate.hbm2ddl.auto=update` can be helpful during development but it's recommended to use `validate` in production.

Transaction and concurrency notes:
- The app opens short-lived sessions for each operation and uses transactions for operations that change the DB. This ensures ACID correctness for each user action.

---

## 7. Data model and normalization

Normalized design decisions:

- Entities map to relational tables as follows:
  - `User` -> `users`
  - `Task` -> `tasks`
  - `UserPreference` -> `user_preferences`

- Normalization level:
  - `users` table: 3NF — attributes are atomic (`email`, `password`, `display_name`, timestamps). `email` enforces uniqueness.
  - `tasks` table: 3NF — `user_id` is a foreign key to `users(id)`. `priority` stored as text (denormalized), which is acceptable here — if priority had complex attributes, it would be normalized into its own table; for simple enumerations (High/Medium/Low) text is acceptable.
  - `user_preferences` table: 3NF — key/value store per user, avoids adding many nullable columns to `users`.

Referential integrity and constraints:
- `tasks.user_id` references `users(id)` with `ON DELETE CASCADE` in the DDL.
- `user_preferences.user_id` references `users(id)` with `ON DELETE CASCADE`.
- Unique constraint on `users.email` is enforced (DDL contains `ALTER TABLE users ADD CONSTRAINT uq_users_email UNIQUE (email)`).

Trade-offs and denormalization:
- `priority` is stored as VARCHAR in `tasks` for simplicity and query performance. If priorities had many attributes or metadata, a `priorities` lookup table would be preferable.

---

## 8. Key code walkthrough with file references and snippets

This section is organized by feature. Each block includes explanation, the calling flow, and the file(s) where the code lives.

### 8.1 Startup and configuration

- Flow: `MainApp.main()` -> `MainApp.start()` -> `ConfigManager.getInstance()` -> `DatabaseService.getInstance()` -> `SceneRouter.init()`
- Files:
  - `src/main/java/com/taskmanager/MainApp.java`
  - `src/main/java/com/taskmanager/config/ConfigManager.java`
  - `src/main/java/com/taskmanager/service/DatabaseService.java`
  - `src/main/java/com/taskmanager/controller/SceneRouter.java`

Snippet (MainApp start):
- File: `src/main/java/com/taskmanager/MainApp.java`
- Method: `start(Stage primaryStage)` — initializes ConfigManager, DatabaseService and SceneRouter.

Additional reference (ConfigManager usage):

```java
// File: src/main/java/com/taskmanager/config/ConfigManager.java
ConfigManager configManager = ConfigManager.getInstance();
configManager.printConfigStatus();
```

### 8.2 Authentication and user lifecycle

- Login flow:
  - `LoginController.onLogin()` reads `emailField` and `passwordField`, calls `DatabaseService.authenticateUser(email, password)`.
  - If authentication succeeds, `SceneRouter.setCurrentUser(user)` and `sceneRouter.showDashboard()` are invoked.
- Files:
  - `src/main/java/com/taskmanager/controller/LoginController.java`
  - `src/main/java/com/taskmanager/service/DatabaseService.java` (method: `authenticateUser`)
  - `src/main/java/com/taskmanager/controller/SceneRouter.java`

Important code (DatabaseService.authenticateUser):
- File: `src/main/java/com/taskmanager/service/DatabaseService.java`
- Query: `FROM User WHERE email = :email AND password = :password`
- Note: Passwords are currently stored/checked in plaintext — see Security section for recommended changes (BCrypt hashing).

Inline HQL examples (from `DatabaseService`):

```java
// find user by email/password
Query<User> q = session.createQuery("FROM User WHERE email = :email AND password = :password", User.class);

// list tasks for a user ordered by creation
Query<Task> tq = session.createQuery("FROM Task WHERE user.id = :userId ORDER BY createdAt DESC", Task.class);
```

### 8.3 Task CRUD

- Create:
  - UI: Dashboard (Add Task dialog) — `DashboardController.showAddTaskDialog()` collects details and calls `taskController.addTask(...)`.
  - Controller: `TaskController.addTask(...)` constructs a `Task` object and calls `databaseService.saveTask(task)`.
  - Persistence: `DatabaseService.saveTask(Task)` merges and commits.
- Read:
  - `TaskController.loadUserTasks()` calls `databaseService.getUserTasks(currentUser.getId())`.
  - `DatabaseService.getUserTasks(int)` runs HQL: `FROM Task WHERE user.id = :userId ORDER BY createdAt DESC`.
- Update:
  - Editing in Dashboard uses `TaskController.updateTask(Task)` -> `databaseService.updateTask(task)`.
- Delete:
  - `TaskController.deleteTask(Task)` -> `databaseService.deleteTask(task.getId())`.

Files involved:
- UI/Dialog: `src/main/java/com/taskmanager/controller/DashboardController.java` (methods: `showAddTaskDialog`, `showEditTaskDialog`, `showDeleteConfirmation`)
- Controller: `src/main/java/com/taskmanager/controller/TaskController.java`
- Persistence: `src/main/java/com/taskmanager/service/DatabaseService.java`

### 8.4 Preferences

- Flow: `DatabaseService.saveUserPreference(userId, key, value)` — queries for existing preference and either `merge`es or `persist`s.
- Files:
  - `src/main/java/com/taskmanager/service/DatabaseService.java` (methods: `saveUserPreference`, `getUserPreference`, `getUserPreferences`)
  - `src/main/java/com/taskmanager/entity/UserPreference.java`

### 8.5 UI rendering & routing

- `SceneRouter` is responsible for loading FXML views and wiring controllers. It also holds the `TaskController` instance and sets the current user when login succeeds.
- Files:
  - `src/main/java/com/taskmanager/controller/SceneRouter.java`
  - Views: `src/main/resources/fxml/LoginView.fxml`, `src/main/resources/fxml/DashboardView.fxml`

Example flow when a user logs in successfully:
1. `LoginController.onLogin()` calls `DatabaseService.authenticateUser(...)`.
2. On success: `sceneRouter.setCurrentUser(user)` and `sceneRouter.showDashboard()`.
3. `SceneRouter.showDashboard()` loads `DashboardView.fxml`, gets `DashboardController`, injects `sceneRouter` and `taskController`, and calls `controller.setCurrentUser(user)` and `controller.setTaskController(taskController)`.
4. `DashboardController.setTaskController` calls `taskController.getTasks()` and sets the `TableView` items.

### 8.6 Examples of HQL/Queries

- Authenticate user:
  - `FROM User WHERE email = :email AND password = :password` (file: `DatabaseService.authenticateUser`)
- Load user tasks:
  - `FROM Task WHERE user.id = :userId ORDER BY createdAt DESC` (file: `DatabaseService.getUserTasks`)
- Save preference: find existing by key `FROM UserPreference WHERE user.id = :userId AND key = :key` (file: `DatabaseService.saveUserPreference`)

---

## 9. How the UI works (JavaFX, controllers, FXML)

Structure:
- Views are defined in FXML under `src/main/resources/fxml/`.
- Each FXML has a controller class under `com.taskmanager.controller` which is registered by the FXML loader.

Key controllers:
- `LoginController` — handles login form, validation, opening registration dialog. File: `src/main/java/com/taskmanager/controller/LoginController.java`.
- `DashboardController` — main application UI, shows tasks table, filters, search, add/edit dialogs. File: `src/main/java/com/taskmanager/controller/DashboardController.java`.
- `TaskController` — non-FXML controller that encapsulates task operations and is injected into `DashboardController`.
- `SceneRouter` — responsible for swapping scenes and passing shared dependencies.

UI data flow example: editing a task
1. User clicks Edit in the Dashboard (actions column in `TableView`).
2. `DashboardController.showEditTaskDialog(task)` opens a dialog pre-populated with task data.
3. On Save, it updates the `Task` object in-memory and calls `taskController.updateTask(task)`.
4. `TaskController.updateTask` calls `DatabaseService.updateTask(task)` to persist changes.
5. Upon success, the `ObservableList<Task>` updates the UI automatically.

---

## 10. Security and production notes

- Password handling: currently passwords are stored and compared as plaintext. This is insecure. Replace with BCrypt hashing:
  - Use `BCrypt.hashpw(password, BCrypt.gensalt())` when storing.
  - Use `BCrypt.checkpw(plainPassword, hashedPassword)` at login.
  - Recommended library: `org.springframework.security:spring-security-crypto` or `de.svenkubiak:jBCrypt`.

- DB user & privileges: do not use the `system` DBA account. Create a dedicated user (example in `create_schema_oracle.sql`): `task_app` with limited grants (CONNECT, CREATE TABLE, CREATE SEQUENCE, etc.).

- Hibernate `hbm2ddl.auto`: set to `validate` in production so Hibernate will not modify schema at startup.

- Secrets: remove DB password from repo and read from environment variables or external secure vault in production.

---

## 11. How to run, test and demonstrate (presentation-ready steps)

Prerequisites:
- JDK 17+, Maven
- Oracle XE (or adapt `hibernate.cfg.xml` to H2/Postgres for a local demo)

Steps to run locally with Oracle (recommended approach):
1. Create application DB user (run as SYS/SYSTEM):

```sql
CREATE USER task_app IDENTIFIED BY "ChangeMeStrongPwd1";
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE TO task_app;
GRANT UNLIMITED TABLESPACE TO task_app; -- or give quotas as needed
```

2. Connect as `task_app` and run the DDL script `src/main/resources/sql/create_schema_oracle.sql`.

3. Edit `src/main/resources/hibernate.cfg.xml` to set `hibernate.connection.username` and `hibernate.connection.password` to `task_app` credentials.

4. Build and run:

```powershell
mvn clean package -DskipTests
mvn javafx:run
```

5. Demonstration checklist:
- Show login screen (`LoginView.fxml`) — register a user via the register dialog.
- Create several tasks with different priorities and deadlines.
- Show filtering by priority, status, and search.
- Mark a task completed and show the change in UI and DB (query `tasks` table).
- Show preferences being set and persisted (query `user_preferences`).

---

## 12. Appendix: relevant files and direct references

- Entry / Boot
  - `src/main/java/com/taskmanager/MainApp.java`
- Configuration
  - `src/main/java/com/taskmanager/config/ConfigManager.java`
  - `src/main/resources/application.properties`
  - `src/main/resources/hibernate.cfg.xml`
- Persistence
  - `src/main/java/com/taskmanager/util/HibernateUtil.java`
  - `src/main/java/com/taskmanager/service/DatabaseService.java`
  - `src/main/resources/sql/create_schema_oracle.sql`
- Entities
  - `src/main/java/com/taskmanager/entity/User.java`
  - `src/main/java/com/taskmanager/entity/Task.java`
  - `src/main/java/com/taskmanager/entity/UserPreference.java`
- Controllers & UI
  - `src/main/java/com/taskmanager/controller/LoginController.java`
  - `src/main/java/com/taskmanager/controller/DashboardController.java`
  - `src/main/java/com/taskmanager/controller/TaskController.java`
  - `src/main/java/com/taskmanager/controller/SceneRouter.java`
  - `src/main/resources/fxml/LoginView.fxml`
  - `src/main/resources/fxml/DashboardView.fxml`
  - `src/main/resources/css/application.css`

---

If you'd like, I can:
- Export this `PROJECT_PRESENTATION.md` to `.docx` and place it in `docs/report/` (I can run the Java converter or you can use Pandoc locally).
- Turn sections into a slide deck (PowerPoint) using Pandoc or a generator.
- Produce a one-page summary slide for your instructor.

Tell me which export or follow-up you'd like and I'll continue.