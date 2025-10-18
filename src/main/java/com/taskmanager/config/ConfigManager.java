package com.taskmanager.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {
    private static ConfigManager instance;
    private Properties properties;
    
    private ConfigManager() {
        loadProperties();
    }
    
    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }
    
    private void loadProperties() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            System.err.println("Could not load application.properties, using defaults");
        }
    }
    
    public String getDbUrl() {
        return getProperty("db.url", "jdbc:oracle:thin:@localhost:1521:xe");
    }
    
    public String getDbUsername() {
        return getProperty("db.username", "system");
    }
    
    public String getDbPassword() {
        return getProperty("db.password", "Meenakshi@10");
    }
    
    public String getDbDriver() {
        return getProperty("db.driver", "oracle.jdbc.driver.OracleDriver");
    }
    
    public String getHibernateDialect() {
        return getProperty("hibernate.dialect", "org.hibernate.community.dialect.Oracle12cDialect");
    }
    
    public String getHibernateHbm2ddl() {
        return getProperty("hibernate.hbm2ddl.auto", "update");
    }
    
    public boolean isHibernateShowSql() {
        return Boolean.parseBoolean(getProperty("hibernate.show_sql", "true"));
    }
    
    public boolean isHibernateFormatSql() {
        return Boolean.parseBoolean(getProperty("hibernate.format_sql", "true"));
    }
    
    public int getHibernatePoolSize() {
        return Integer.parseInt(getProperty("hibernate.connection.pool_size", "10"));
    }
    
    public String getAppTitle() {
        return getProperty("app.title", "Task Management & To-Do Application");
    }
    
    public String getAppVersion() {
        return getProperty("app.version", "1.0.0");
    }
    
    private String getProperty(String key, String defaultValue) {
        // First check environment variables
        String envValue = System.getenv(key.toUpperCase().replace(".", "_"));
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }
        
        // Then check system properties
        String sysValue = System.getProperty(key);
        if (sysValue != null && !sysValue.isEmpty()) {
            return sysValue;
        }
        
        // Finally check properties file
        return properties.getProperty(key, defaultValue);
    }
    
    public void printConfigStatus() {
        System.out.println("=== Configuration Status ===");
        System.out.println("Database URL: " + getDbUrl());
        System.out.println("Database Username: " + getDbUsername());
        System.out.println("Database Driver: " + getDbDriver());
        System.out.println("Hibernate Dialect: " + getHibernateDialect());
        System.out.println("Hibernate HBM2DDL: " + getHibernateHbm2ddl());
        System.out.println("Show SQL: " + isHibernateShowSql());
        System.out.println("Format SQL: " + isHibernateFormatSql());
        System.out.println("Connection Pool Size: " + getHibernatePoolSize());
        System.out.println("App Title: " + getAppTitle());
        System.out.println("App Version: " + getAppVersion());
        System.out.println("============================");
    }
}
