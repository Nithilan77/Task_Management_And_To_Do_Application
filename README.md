# Task Management & To-Do Application

This is a lightweight JavaFX desktop Task Management and To‑Do application using Hibernate (JPA) for persistence. It is built for demonstration and academic submission: clean UI, modular architecture, and manual database schema support for production safety.

Contents
- `src/main/java` — source code (entities, controllers, services, utils)
- `src/main/resources` — resources (FXML, CSS, `hibernate.cfg.xml`, SQL scripts)
- `src/main/resources/sql/create_schema_oracle.sql` — Oracle-compatible DDL (sequences, tables, triggers)
- `docs/images` — ER & architecture diagrams (SVG)
- `PROJECT_REPORT_DETAILED.md` — expanded project report (15-page-ready; convert to .docx)
- `PROJECT_CODE_SNIPPETS.md` — key code snippets and explanations

Prerequisites
- Java 17+ (JDK)
- Maven 3.6+
- Oracle XE (or other RDBMS). For quick testing you can adapt `hibernate.cfg.xml` for H2 or Postgres.

Quick start (developer)
1. Configure database in `src/main/resources/hibernate.cfg.xml` (username/password/url).
2. If you want to create schema manually (recommended for production), run the DDL script in `src/main/resources/sql/create_schema_oracle.sql` as a DBA or application user.
3. Build and run:

```powershell
mvn clean package -DskipTests
mvn javafx:run
```

Database schema (manual setup)
- Recommended: create a dedicated schema/user `task_app` and grant minimal privileges. Example (run as SYSTEM or DBA):

```sql
CREATE USER task_app IDENTIFIED BY "ChangeMeStrongPwd1";
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE TO task_app;
-- optionally grant UNLIMITED TABLESPACE or a specific quota
```

- Connect as `task_app` and run the SQL script `src/main/resources/sql/create_schema_oracle.sql`. The script creates sequences (`TASK_SEQ`, `USER_SEQ`, `PREFERENCE_SEQ`), tables (`users`, `tasks`, `user_preferences`), FK constraints, triggers, and verification queries.

Why manual DDL?
- The project was initially configured with `hibernate.hbm2ddl.auto=update` which can alter schemas automatically. For production or evaluation, manual DDL is safer and avoids accidental data loss or schema drift. Use `validate` in production to ensure mappings match the schema without modifying it.

To switch Hibernate to validate-only mode, in `src/main/resources/hibernate.cfg.xml` change:

```xml
<property name="hibernate.hbm2ddl.auto">update</property>
```

to:

```xml
<property name="hibernate.hbm2ddl.auto">validate</property>
```

Generating the submission report (.docx)
- I included a Markdown report `PROJECT_REPORT_DETAILED.md` that is ready for conversion to `.docx`.
- Option A (recommended): Install Pandoc on your machine and run the provided PowerShell helper:

```powershell
# install Pandoc (one-time)
winget install --id=Pandoc.Pandoc -e
# convert
cd 'C:\Users\user\IdeaProjects\Task Management and To-Do Application'
docs\report\convert_to_docx.ps1 -Input '..\PROJECT_REPORT_DETAILED.md' -Output 'PROJECT_REPORT_DETAILED.docx'
```

- Option B: I added a Java converter (`ReportToDocx`) and necessary build configuration. If you prefer, run the converter via Maven (I can help run it here or produce the JAR):

```powershell
mvn -DskipTests package
mvn exec:java -Dexec.mainClass=com.taskmanager.util.ReportToDocx
# OR build a shaded jar (profile included):
#mvn -Pmake-fat-jar -DskipTests package
#java -jar target\TaskManagementApp-1.0-SNAPSHOT-shaded.jar
```

Notes and security
- Never commit production DB credentials. Replace `system` in `hibernate.cfg.xml` with a dedicated application user and use environment variables for secrets.
- Replace plaintext password storage with BCrypt before any production use.

Packaging recommendations
- For distribution, consider using `jpackage` or `jlink` to create a self-contained runtime image.

Contributing & Contact
- Fork the repo, create a feature branch, and open a PR. Include tests for new behavior.

License
- Add your preferred license (e.g., MIT) to `LICENSE` if you intend to publish this repository.

Support
- If you want me to produce the `.docx` for you directly (I can run the converter and place the file in `docs/report/`), say so and I will run the converter and provide the file.
# Task Management & To-Do Application

An elegant desktop Task Management and To-Do application built with Java, JavaFX (FXML) and Hibernate (JPA) backed persistence. The UI ships with a refined, professional stylesheet and a clean component structure for easy customization.

## Table of contents
- Features
- Tech stack
- Quick start (build & run)
- Run from IDE
- Project structure
- Styling and theming
- Database
- Packaging
- Contributing
- Troubleshooting

## Features
- Add, edit and remove tasks
- Task priorities and deadlines
- User entity and preferences
- Responsive-ish JavaFX layout with polished CSS theme
- Dialogs for create/edit tasks

## Tech stack
- Java 11+ (or your project's configured JDK)
- Maven build system (`pom.xml`)
- JavaFX for UI (FXML under `src/main/resources/fxml`)
- Hibernate / JPA for persistence

## Quick start (command line)
1. Ensure you have a JDK installed (11+ recommended) and `mvn` on PATH.
2. From project root:

```powershell
mvn -DskipTests package
mvn javafx:run
```

The first command compiles and packages the project (tests skipped). The second launches the JavaFX application.

If `mvn javafx:run` fails due to missing JavaFX modules, ensure you have JavaFX dependencies available. This project is configured to use the JavaFX Maven plugin in `pom.xml`.

## Run from IDE (IntelliJ IDEA recommended)
1. Open the project in IntelliJ IDEA (or other Java IDE).
2. Ensure your project SDK is set (Project Structure → SDKs).
3. Build the project (Maven tool window → Lifecycle → package) or use the Run configuration for the main class (`com.taskmanager.MainApp` / `MainApp.java`).

## Project structure

- `pom.xml` — Maven configuration
- `src/main/java` — Java sources
  - `com.taskmanager` — entry and config
  - `controller` — JavaFX controllers (Login, Dashboard, Task controllers)
  - `entity` — JPA entities (Task, User, UserPreference)
  - `service` — database access / services
  - `util` — utility and Hibernate helper classes
- `src/main/resources` — resources used by the app
  - `fxml/` — JavaFX FXML views (LoginView, DashboardView)
  - `css/application.css` — main stylesheet (elegant theme)
  - `hibernate.cfg.xml` & `database-init.sql` — DB config and initial data

## Styling and theme
The UI uses a modern, elegant stylesheet at `src/main/resources/css/application.css`. Modify the accent color and spacing near the top of the file to adapt the look-and-feel. The stylesheet aims to be compatible with JavaFX CSS; any IDE lint warnings about standard CSS properties are safe to ignore because JavaFX uses `-fx-` prefixed properties.

## Database
- By default the project contains `hibernate.cfg.xml` for configuring the database connection. There is a `database-init.sql` file used to pre-populate or initialize schema/data.
- Check `com.taskmanager.service.DatabaseService` and `com.taskmanager.util.HibernateUtil` for persistence setup. If you intend to use a production database, update `hibernate.cfg.xml` and the JDBC URL/credentials.

## Packaging
- Use `mvn package` to build a JAR. For distribution as a platform specific native package, consider using jlink or jpackage (not included here).

## Push to GitHub (if you want to publish the repo)
If you have not yet created a repository on GitHub, create one named `Task_Management_And_To_Do_Application` (or a different name). Then push your local commits:

```powershell
# replace USER/REPO with your GitHub username and chosen repo name
git remote set-url origin https://github.com/USER/REPO.git
git push -u origin main
```

If the push fails with `remote: Repository not found`, create the repository in the GitHub web UI first (or generate a personal access token and use it as your password for HTTPS pushes). If you'd like, I can help automate repo creation using a PAT.

## Contributing
- Fork or clone, make changes, open a PR. Add tests under `src/test/java` if you change behavior.

## Troubleshooting
- `java.lang.module` errors when running JavaFX: ensure required JavaFX modules are available and matched to your JDK. Using the Maven JavaFX plugin typically resolves this.
- Build fails with missing dependencies: run `mvn -U -DskipTests package` to force an update.
- CSS lint warnings in your IDE about standard CSS properties: safe to ignore — JavaFX uses `-fx-` prefixed properties.
- Git push `Repository not found`: ensure remote URL points to an existing repository or create one on GitHub and then push.

If you want, I can:
- Create the GitHub repository programmatically (requires a PAT) and push the current branch.
- Add a LICENSE file (MIT/Apache/GPL) and CI (GitHub Actions) for builds.
- Tweak the UI theme further or produce example screenshots.

---
If you'd like, tell me which next step to perform (create remote and push, add CI, add license, or refine the UI further). 
# Task_Management_And_To_Do_Application
