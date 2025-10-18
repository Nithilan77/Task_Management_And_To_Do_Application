-- Database initialization script for Task Management Application
-- Run this script in Oracle Database before starting the application

-- Create sequences for auto-incrementing IDs
CREATE SEQUENCE USER_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE TASK_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE PREFERENCE_SEQ START WITH 1 INCREMENT BY 1;

-- Grant necessary permissions (if needed)
-- GRANT CREATE SESSION TO system;
-- GRANT CREATE TABLE TO system;
-- GRANT CREATE SEQUENCE TO system;
