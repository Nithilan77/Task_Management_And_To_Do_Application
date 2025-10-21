# Project Code Snippets — Task Management & To-Do Application

This file collects the key code snippets from the project with brief explanations. Use this as a concise developer reference and to include in your submission.

---

## 1) Entities

### Task.java (important fields and annotations)

```java
package com.taskmanager.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_seq")
    @SequenceGenerator(name = "task_seq", sequenceName = "TASK_SEQ", allocationSize = 1)
    private int id;

    @Column(nullable = false)
    private String title;

    private String description; // mapped to CLOB in DDL

    private String priority; // High, Medium, Low

    private LocalDate deadline;

    private boolean completed;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}
```

Notes:
- Uses SEQUENCE generation; sequences are expected to exist (`TASK_SEQ`).
- `description` may be large — DDL uses CLOB.


### User.java (key fields)

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "USER_SEQ", allocationSize = 1)
    private int id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password; // store hashed password in production

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Task> tasks = new ArrayList<>();
}
```

Notes:
- `password` must be hashed for production (BCrypt recommended). The current implementation stores plaintext; update needed.


### UserPreference.java

```java
@Entity
@Table(name = "user_preferences")
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "preference_seq")
    @SequenceGenerator(name = "preference_seq", sequenceName = "PREFERENCE_SEQ", allocationSize = 1)
    private int id;

    @Column(name = "preference_key", nullable = false)
    private String key;

    @Column(name = "preference_value")
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
```


---

## 2) Persistence utility

### HibernateUtil.java (session factory)

```java
public class HibernateUtil {
    private static SessionFactory sessionFactory;
    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            Configuration cfg = new Configuration().configure(); // reads hibernate.cfg.xml
            StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder()
                .applySettings(cfg.getProperties());
            sessionFactory = cfg.buildSessionFactory(builder.build());
        }
        return sessionFactory;
    }
}
```

Notes:
- Ensures a single `SessionFactory` per JVM. `hibernate.cfg.xml` contains DB connection & dialect.


---

## 3) Service layer

### DatabaseService.java (key methods)

```java
public class DatabaseService {
    public User registerUser(String email, String password, String displayName) {
        User user = new User(email, password, displayName);
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = s.beginTransaction();
            s.persist(user);
            tx.commit();
        }
        return user;
    }

    public Optional<User> authenticateUser(String email, String password) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            User user = s.createQuery("from User where email = :email", User.class)
                         .setParameter("email", email)
                         .uniqueResult();
            if (user != null && user.getPassword().equals(password)) return Optional.of(user);
        }
        return Optional.empty();
    }

    public Task saveTask(Task task) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = s.beginTransaction();
            s.saveOrUpdate(task);
            tx.commit();
        }
        return task;
    }

    public List<Task> getUserTasks(int userId) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("from Task t where t.user.id = :uid", Task.class)
                    .setParameter("uid", userId)
                    .list();
        }
    }
}
```

Notes:
- Each DB operation opens a session and wraps writes in transactions.
- For production, password handling must change to use hashed passwords.


---

## 4) Controllers / UI (high-level snippets)

### LoginController.java (login flow)

```java
public void onLoginPressed() {
    String email = emailField.getText();
    String password = passwordField.getText();
    var optUser = DatabaseService.getInstance().authenticateUser(email, password);
    if (optUser.isPresent()) {
        SceneRouter.getInstance().showDashboard(optUser.get());
    } else {
        showError("Invalid credentials");
    }
}
```

Notes:
- UI actions should run DB calls on background threads to keep JavaFX responsive.


### DashboardController.java (saving a task)

```java
public void onSaveTask() {
    Task task = collectFromForm();
    CompletableFuture.runAsync(() -> {
        databaseService.saveTask(task);
        Platform.runLater(() -> refreshTable());
    });
}
```

Notes:
- Use `CompletableFuture` or JavaFX `Task` to run DB work off the FX thread.


---

## 5) Configuration

### hibernate.cfg.xml (important properties)

- Dialect: `org.hibernate.community.dialect.Oracle12cDialect`
- Currently `hibernate.hbm2ddl.auto` is `update` (careful). For production use `validate` or remove this property.

Example snippet:

```xml
<property name="hibernate.dialect">org.hibernate.community.dialect.Oracle12cDialect</property>
<property name="hibernate.hbm2ddl.auto">update</property>
<property name="hibernate.show_sql">true</property>
```


---

## 6) SQL Schema (excerpt)

See `src/main/resources/sql/create_schema_oracle.sql` for the full Oracle DDL. Important elements:
- Sequences: `TASK_SEQ`, `USER_SEQ`, `PREFERENCE_SEQ`
- Tables: `users`, `tasks`, `user_preferences`
- `tasks.user_id` FK references `users(id)` with ON DELETE CASCADE
- `description` stored as CLOB


---

## 7) Build & Run (developer)

- Build:

```powershell
mvn clean package -DskipTests
```

- Run (dev via JavaFX Maven plugin):

```powershell
mvn javafx:run
```

Notes:
- Configure `src/main/resources/hibernate.cfg.xml` with the correct DB credentials or use an H2 profile for demo.


---

## 8) Security & Next Improvements

- Replace plaintext passwords with BCrypt (`org.mindrot:jbcrypt`) and update registration/login flow.
- Externalize DB credentials (environment variables, encrypted config) and don't commit secrets.
- Add JUnit + H2 integration tests for core service methods.


---

If you want, I can:
- Insert these snippets directly into `README.md` and replace the brief README we added earlier; or
- Expand each snippet with the full source file and line numbers for easy grading.

Tell me which you prefer and I will update files accordingly.