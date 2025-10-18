package com.taskmanager.util;

import com.taskmanager.entity.User;
import com.taskmanager.entity.Task;
import com.taskmanager.service.DatabaseService;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.time.LocalDate;

public class DatabaseTest {
    
    public static void main(String[] args) {
        System.out.println("=== Database Connection Test ===");
        
        try {
            // Test Hibernate connection
            SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
            System.out.println("✅ Hibernate SessionFactory created successfully");
            
            // Test database connection
            try (Session session = sessionFactory.openSession()) {
                System.out.println("✅ Database connection successful");
                
                // Test creating a user
                DatabaseService dbService = DatabaseService.getInstance();
                
                // Create a test user
                User testUser = new User("test@example.com", "password123", "Test User");
                User savedUser = dbService.registerUser(testUser.getEmail(), testUser.getPassword(), testUser.getDisplayName());
                System.out.println("✅ User created successfully with ID: " + savedUser.getId());
                
                // Create a test task
                Task testTask = new Task(
                    "Test Task", 
                    "This is a test task", 
                    "High", 
                    LocalDate.now().plusDays(7), 
                    false, 
                    savedUser
                );
                Task savedTask = dbService.saveTask(testTask);
                System.out.println("✅ Task created successfully with ID: " + savedTask.getId());
                
                // Test retrieving tasks
                var tasks = dbService.getUserTasks(savedUser.getId());
                System.out.println("✅ Retrieved " + tasks.size() + " tasks for user");
                
                // Clean up test data
                dbService.deleteTask(savedTask.getId());
                System.out.println("✅ Test data cleaned up");
                
            } catch (Exception e) {
                System.err.println("❌ Database operation failed: " + e.getMessage());
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Hibernate setup failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            HibernateUtil.shutdown();
        }
        
        System.out.println("=== Test Complete ===");
    }
}
