package com.taskmanager.service;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.entity.UserPreference;
import com.taskmanager.util.HibernateUtil;

public class DatabaseService {
    private static DatabaseService instance;
    
    private DatabaseService() {
        // Configuration is handled by HibernateUtil
    }
    
    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }
    
    // User Management
    public User authenticateUser(String email, String password) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Query<User> query = session.createQuery(
                    "FROM User WHERE email = :email AND password = :password", User.class);
                query.setParameter("email", email);
                query.setParameter("password", password);
                
                User user = query.uniqueResult();
                if (user != null) {
                    user.setLastLogin(LocalDateTime.now());
                    session.merge(user);
                }
                tx.commit();
                return user;
            } catch (Exception e) {
                tx.rollback();
                throw e;
            }
        }
    }
    
    public User registerUser(String email, String password, String displayName) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                // Check if user already exists
                Query<User> existingUserQuery = session.createQuery(
                    "FROM User WHERE email = :email", User.class);
                existingUserQuery.setParameter("email", email);
                User existingUser = existingUserQuery.uniqueResult();
                
                if (existingUser != null) {
                    throw new RuntimeException("User with email " + email + " already exists");
                }
                
                User user = new User(email, password, displayName);
                session.persist(user);
                tx.commit();
                return user;
            } catch (Exception e) {
                tx.rollback();
                throw new RuntimeException("Failed to register user: " + e.getMessage(), e);
            }
        }
    }
    
    public User getUserById(int userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(User.class, userId);
        }
    }
    
    // Task Management
    public Task saveTask(Task task) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                session.merge(task);
                tx.commit();
                return task;
            } catch (Exception e) {
                tx.rollback();
                throw new RuntimeException("Failed to save task: " + e.getMessage(), e);
            }
        }
    }
    
    public List<Task> getUserTasks(int userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Task> query = session.createQuery(
                "FROM Task WHERE user.id = :userId ORDER BY createdAt DESC", Task.class);
            query.setParameter("userId", userId);
            return query.list();
        }
    }
    
    public List<Task> getUserTasksByStatus(int userId, boolean completed) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Task> query = session.createQuery(
                "FROM Task WHERE user.id = :userId AND completed = :completed ORDER BY createdAt DESC", Task.class);
            query.setParameter("userId", userId);
            query.setParameter("completed", completed);
            return query.list();
        }
    }
    
    public List<Task> getUserTasksByPriority(int userId, String priority) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Task> query = session.createQuery(
                "FROM Task WHERE user.id = :userId AND priority = :priority ORDER BY createdAt DESC", Task.class);
            query.setParameter("userId", userId);
            query.setParameter("priority", priority);
            return query.list();
        }
    }
    
    public void deleteTask(int taskId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Task task = session.get(Task.class, taskId);
                if (task != null) {
                    session.remove(task);
                }
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                throw new RuntimeException("Failed to delete task: " + e.getMessage(), e);
            }
        }
    }
    
    public Task updateTask(Task task) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                task.setUpdatedAt(LocalDateTime.now());
                session.merge(task);
                tx.commit();
                return task;
            } catch (Exception e) {
                tx.rollback();
                throw new RuntimeException("Failed to update task: " + e.getMessage(), e);
            }
        }
    }
    
    // User Preferences Management
    public void saveUserPreference(int userId, String key, String value) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                // Check if preference already exists
                Query<UserPreference> existingQuery = session.createQuery(
                    "FROM UserPreference WHERE user.id = :userId AND key = :key", UserPreference.class);
                existingQuery.setParameter("userId", userId);
                existingQuery.setParameter("key", key);
                UserPreference existing = existingQuery.uniqueResult();
                
                if (existing != null) {
                    existing.setValue(value);
                    session.merge(existing);
                } else {
                    User user = session.get(User.class, userId);
                    UserPreference preference = new UserPreference(key, value, user);
                    session.persist(preference);
                }
                
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                throw new RuntimeException("Failed to save user preference: " + e.getMessage(), e);
            }
        }
    }
    
    public String getUserPreference(int userId, String key) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<UserPreference> query = session.createQuery(
                "FROM UserPreference WHERE user.id = :userId AND key = :key", UserPreference.class);
            query.setParameter("userId", userId);
            query.setParameter("key", key);
            UserPreference preference = query.uniqueResult();
            return preference != null ? preference.getValue() : null;
        }
    }
    
    public List<UserPreference> getUserPreferences(int userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<UserPreference> query = session.createQuery(
                "FROM UserPreference WHERE user.id = :userId", UserPreference.class);
            query.setParameter("userId", userId);
            return query.list();
        }
    }
    
    public void close() {
        HibernateUtil.shutdown();
    }
}
