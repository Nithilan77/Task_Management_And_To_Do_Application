-- Schema and objects for Task Management app (Oracle)
-- Run as a DBA (SYSTEM) or a user with CREATE USER and GRANT privileges.
-- Replace passwords and schema names before running.

-- 1) Create an application schema/user (recommended)
-- Run as SYS or SYSTEM:
-- CREATE USER task_app IDENTIFIED BY "ChangeMeStrongPwd1";
-- GRANT CREATE SESSION TO task_app;
-- GRANT CREATE TABLE TO task_app;
-- GRANT CREATE SEQUENCE TO task_app;
-- GRANT CREATE VIEW TO task_app;
-- GRANT CREATE PROCEDURE TO task_app;
-- GRANT UNLIMITED TABLESPACE TO task_app; -- optional, or grant specific quotas

-- Alternatively, if you want to create objects in an existing schema, skip user creation

-- 2) Connect as the application user (or run the following as that user)
-- CONNECT task_app/ChangeMeStrongPwd1@<db_connect_string>

-- 3) Create sequences used by JPA annotations
CREATE SEQUENCE TASK_SEQ START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE USER_SEQ START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE PREFERENCE_SEQ START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

-- 4) Create tables

-- Users table
CREATE TABLE users (
    id NUMBER(10) PRIMARY KEY,
    email VARCHAR2(255) NOT NULL,
    password VARCHAR2(255) NOT NULL,
    display_name VARCHAR2(255),
    created_at TIMESTAMP,
    last_login TIMESTAMP
);

-- Unique constraint on email
ALTER TABLE users ADD CONSTRAINT uq_users_email UNIQUE (email);

-- Tasks table
CREATE TABLE tasks (
    id NUMBER(10) PRIMARY KEY,
    title VARCHAR2(400) NOT NULL,
    description CLOB,
    priority VARCHAR2(50),
    deadline DATE,
    completed NUMBER(1) DEFAULT 0 NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    user_id NUMBER(10) NOT NULL
);

-- Foreign key to users
ALTER TABLE tasks ADD CONSTRAINT fk_tasks_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Index for tasks.user_id
CREATE INDEX idx_tasks_user_id ON tasks(user_id);

-- User preferences table
CREATE TABLE user_preferences (
    id NUMBER(10) PRIMARY KEY,
    preference_key VARCHAR2(255) NOT NULL,
    preference_value VARCHAR2(2000),
    created_at TIMESTAMP,
    user_id NUMBER(10) NOT NULL
);

-- Foreign key to users
ALTER TABLE user_preferences ADD CONSTRAINT fk_prefs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Index for preferences.user_id
CREATE INDEX idx_prefs_user_id ON user_preferences(user_id);

-- 5) Optional: triggers to set id from sequence if inserts originate outside Hibernate
-- (Hibernate normally fetches NEXTVAL itself; triggers are optional.)

-- Trigger for tasks
CREATE OR REPLACE TRIGGER trg_tasks_before_insert
BEFORE INSERT ON tasks
FOR EACH ROW
BEGIN
  IF :NEW.id IS NULL THEN
    SELECT TASK_SEQ.NEXTVAL INTO :NEW.id FROM DUAL;
  END IF;
END;
/

-- Trigger for users
CREATE OR REPLACE TRIGGER trg_users_before_insert
BEFORE INSERT ON users
FOR EACH ROW
BEGIN
  IF :NEW.id IS NULL THEN
    SELECT USER_SEQ.NEXTVAL INTO :NEW.id FROM DUAL;
  END IF;
END;
/

-- Trigger for user_preferences
CREATE OR REPLACE TRIGGER trg_prefs_before_insert
BEFORE INSERT ON user_preferences
FOR EACH ROW
BEGIN
  IF :NEW.id IS NULL THEN
    SELECT PREFERENCE_SEQ.NEXTVAL INTO :NEW.id FROM DUAL;
  END IF;
END;
/
-- 6) Optional: enforce boolean semantics for 'completed' column (0/1) via check constraint
ALTER TABLE tasks ADD CONSTRAINT chk_tasks_completed CHECK (completed IN (0,1));

-- 7) Verification queries (run as the application user)
-- List tables
-- SELECT table_name FROM user_tables WHERE table_name IN ('USERS','TASKS','USER_PREFERENCES');

-- Check sequences
-- SELECT sequence_name, last_number FROM user_sequences WHERE sequence_name IN ('TASK_SEQ','USER_SEQ','PREFERENCE_SEQ');

-- Basic sample inserts to test sequences and FK constraints
-- INSERT INTO users(email, password, display_name, created_at) VALUES ('alice@example.com', 'secret', 'Alice', CURRENT_TIMESTAMP);
-- INSERT INTO tasks(title, description, priority, deadline, completed, created_at, user_id) VALUES ('Test task', 'desc', 'High', TO_DATE('2025-12-31', 'YYYY-MM-DD'), 0, CURRENT_TIMESTAMP, 1);

-- End of script
