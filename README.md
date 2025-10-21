# Task Management & To‑Do Application

An elegant desktop Task Management and To‑Do application built with Java, JavaFX (FXML) and Hibernate (JPA). This repo contains source code, configuration, diagrams, and submission materials prepared for academic evaluation.

---

## Table of contents

- [Features](#features)
- [Tech stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Quick start (CLI)](#quick-start-cli)
- [Run from IDE](#run-from-ide)
- [Project structure](#project-structure)
- [Database setup (manual DDL)](#database-setup-manual-ddl)
- [Configuration and Hibernate mode](#configuration-and-hibernate-mode)
- [Generate submission report (.docx)](#generate-submission-report-docx)
- [Styling & screenshots](#styling--screenshots)
- [Packaging](#packaging)
- [Security notes](#security-notes)
- [Contributing](#contributing)
- [Troubleshooting](#troubleshooting)

---

## Features

- Create, edit and delete tasks (title, description, priority, deadline, completed)
- Per-user accounts and preferences
- Filtering and sorting of tasks
- Clean JavaFX UI with an elegant CSS theme

## Tech stack

- Java 17+
- JavaFX (FXML)
- Hibernate 6.x (JPA)
- Maven
- Oracle (DDL provided) — adaptable to H2/Postgres for tests

## Prerequisites

- JDK 17 or newer installed and on PATH
- Maven 3.6+ on PATH
- Oracle XE (optional for production testing) or configure H2/Postgres for local tests

## Quick start (CLI)

1. Configure database credentials in `src/main/resources/hibernate.cfg.xml`.
2. (Optional / recommended) Run manual DDL script to create schema:

```powershell
# as DBA or using SQL Developer / SQL*Plus
# connect as a DBA and create an application user (example):
CREATE USER task_app IDENTIFIED BY "ChangeMeStrongPwd1";
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE TO task_app;

# connect as task_app and run:
-- run SQL script src/main/resources/sql/create_schema_oracle.sql
```

3. Build and run the app (development):

```powershell
mvn clean package -DskipTests
mvn javafx:run
```

If `mvn javafx:run` fails due to JavaFX module issues, ensure JavaFX dependencies are properly available for your JDK or run from your IDE with the JavaFX SDK configured.

## Run from IDE (IntelliJ IDEA recommended)

1. Open the project in IntelliJ IDEA.
2. Ensure Project SDK is set to JDK 17+ (File → Project Structure).
3. Run the main class `com.taskmanager.MainApp` or use the Maven `javafx:run` configuration.

## Project structure

- `pom.xml` — Maven build
- `src/main/java` — application source code
  - `com.taskmanager` — application entry and configuration
  - `com.taskmanager.controller` — JavaFX controllers
  - `com.taskmanager.entity` — JPA entities (`Task`, `User`, `UserPreference`)
  - `com.taskmanager.service` — `DatabaseService` and business logic
  - `com.taskmanager.util` — `HibernateUtil`, tools
- `src/main/resources` — FXML, CSS, `hibernate.cfg.xml`, SQL scripts
- `docs/` — diagrams and reports

## Database setup (manual DDL)

The repository includes `src/main/resources/sql/create_schema_oracle.sql` which:
- creates sequences `TASK_SEQ`, `USER_SEQ`, `PREFERENCE_SEQ`
- creates tables `users`, `tasks`, `user_preferences`
- creates FK constraints and example triggers

Run that script as the application schema owner (recommended). Manual DDL is safer than letting Hibernate perform schema updates in production.

## Configuration and Hibernate mode

Edit `src/main/resources/hibernate.cfg.xml` to update JDBC URL, username, and password.

To prevent Hibernate from altering the database schema automatically, change:

```xml
<property name="hibernate.hbm2ddl.auto">update</property>
```

to:

```xml
<property name="hibernate.hbm2ddl.auto">validate</property>
```

`validate` makes Hibernate check mappings against the schema and fail early if something is wrong, but it will not modify the schema.

## Styling & screenshots

- The main stylesheet is `src/main/resources/css/application.css`. Tweak variables at the top to change accents and spacing.
- Save screenshots under `docs/screenshots/` with descriptive names (`login.png`, `dashboard.png`) and reference them in the report.

## Packaging

- Build the JAR with `mvn package`.
- For native installers consider `jpackage` or `jlink` to bundle the JRE and assets.

## Security notes

- Do not commit production DB credentials. Replace `system` in `hibernate.cfg.xml` with a dedicated user and prefer environment variables or an external config file not tracked by Git.
- Replace plaintext password storage with a secure password hashing mechanism (BCrypt) before production use.
