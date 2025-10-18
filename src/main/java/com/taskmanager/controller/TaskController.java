package com.taskmanager.controller;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.service.DatabaseService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class TaskController {
    private final DatabaseService databaseService;
    private User currentUser;
    private final ObservableList<Task> tasks;
    
    public TaskController() {
        this.databaseService = DatabaseService.getInstance();
        this.tasks = FXCollections.observableArrayList();
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadUserTasks();
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public ObservableList<Task> getTasks() {
        return tasks;
    }
    
    public void loadUserTasks() {
        if (currentUser != null) {
            List<Task> userTasks = databaseService.getUserTasks(currentUser.getId());
            tasks.clear();
            tasks.addAll(userTasks);
        }
    }
    
    public void loadTasksByStatus(boolean completed) {
        if (currentUser != null) {
            List<Task> filteredTasks = databaseService.getUserTasksByStatus(currentUser.getId(), completed);
            tasks.clear();
            tasks.addAll(filteredTasks);
        }
    }
    
    public void loadTasksByPriority(String priority) {
        if (currentUser != null) {
            List<Task> filteredTasks = databaseService.getUserTasksByPriority(currentUser.getId(), priority);
            tasks.clear();
            tasks.addAll(filteredTasks);
        }
    }
    
    public void addTask(String title, String description, String priority, LocalDate deadline) {
        if (currentUser == null) {
            showAlert("Error", "No user logged in", AlertType.ERROR);
            return;
        }
        
        if (title == null || title.trim().isEmpty()) {
            showAlert("Error", "Task title is required", AlertType.ERROR);
            return;
        }
        
        try {
            Task task = new Task(
                title.trim(),
                description != null ? description.trim() : "",
                priority != null ? priority : "Medium",
                deadline,
                false,
                currentUser
            );
            
            databaseService.saveTask(task);
            tasks.add(0, task); // Add to beginning of list
            showAlert("Success", "Task added successfully", AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Error", "Failed to add task: " + e.getMessage(), AlertType.ERROR);
        }
    }
    
    public void updateTask(Task task) {
        if (currentUser == null) {
            showAlert("Error", "No user logged in", AlertType.ERROR);
            return;
        }
        
        try {
            databaseService.updateTask(task);
            // The task is already in the observable list, so it will update automatically
            showAlert("Success", "Task updated successfully", AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Error", "Failed to update task: " + e.getMessage(), AlertType.ERROR);
        }
    }
    
    public void deleteTask(Task task) {
        if (currentUser == null) {
            showAlert("Error", "No user logged in", AlertType.ERROR);
            return;
        }
        
        try {
            databaseService.deleteTask(task.getId());
            tasks.remove(task);
            showAlert("Success", "Task deleted successfully", AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Error", "Failed to delete task: " + e.getMessage(), AlertType.ERROR);
        }
    }
    
    public void toggleTaskCompletion(Task task) {
        if (currentUser == null) {
            showAlert("Error", "No user logged in", AlertType.ERROR);
            return;
        }
        
        try {
            task.setCompleted(!task.isCompleted());
            databaseService.updateTask(task);
            // The task is already in the observable list, so it will update automatically
        } catch (Exception e) {
            showAlert("Error", "Failed to update task: " + e.getMessage(), AlertType.ERROR);
        }
    }
    
    public void searchTasks(String searchTerm) {
        if (currentUser == null) {
            return;
        }
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            loadUserTasks();
            return;
        }
        
        List<Task> allTasks = databaseService.getUserTasks(currentUser.getId());
        List<Task> filteredTasks = allTasks.stream()
            .filter(task -> 
                task.getTitle().toLowerCase().contains(searchTerm.toLowerCase()) ||
                (task.getDescription() != null && 
                 task.getDescription().toLowerCase().contains(searchTerm.toLowerCase()))
            )
            .collect(Collectors.toList());
        
        tasks.clear();
        tasks.addAll(filteredTasks);
    }
    
    public void clearCompletedTasks() {
        if (currentUser == null) {
            showAlert("Error", "No user logged in", AlertType.ERROR);
            return;
        }
        
        try {
            List<Task> completedTasks = databaseService.getUserTasksByStatus(currentUser.getId(), true);
            for (Task task : completedTasks) {
                databaseService.deleteTask(task.getId());
            }
            loadUserTasks(); // Reload all tasks
            showAlert("Success", "Completed tasks cleared successfully", AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Error", "Failed to clear completed tasks: " + e.getMessage(), AlertType.ERROR);
        }
    }
    
    public int getTaskCount() {
        return tasks.size();
    }
    
    public int getCompletedTaskCount() {
        return (int) tasks.stream().filter(Task::isCompleted).count();
    }
    
    public int getPendingTaskCount() {
        return (int) tasks.stream().filter(task -> !task.isCompleted()).count();
    }
    
    public int getHighPriorityTaskCount() {
        return (int) tasks.stream()
            .filter(task -> "High".equals(task.getPriority()) && !task.isCompleted())
            .count();
    }
    
    public int getOverdueTaskCount() {
        LocalDate today = LocalDate.now();
        return (int) tasks.stream()
            .filter(task -> !task.isCompleted() && 
                           task.getDeadline() != null && 
                           task.getDeadline().isBefore(today))
            .count();
    }
    
    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
