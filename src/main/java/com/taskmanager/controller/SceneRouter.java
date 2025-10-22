package com.taskmanager.controller;

import java.io.IOException;

import com.taskmanager.config.ConfigManager;
import com.taskmanager.entity.User;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class SceneRouter {
    private static SceneRouter instance;
    private Stage primaryStage;
    private User currentUser;
    private final TaskController taskController;
    private final ConfigManager configManager;
    
    private SceneRouter() {
        this.taskController = new TaskController();
        this.configManager = ConfigManager.getInstance();
    }
    
    public static SceneRouter getInstance() {
        if (instance == null) {
            instance = new SceneRouter();
        }
        return instance;
    }
    
    public void init(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle(configManager.getAppTitle());
        this.primaryStage.setWidth(1200);
        this.primaryStage.setHeight(800);
        this.primaryStage.setMinWidth(800);
        this.primaryStage.setMinHeight(600);
        
        // Apply styles
        applyStyles();
        
        // Show login screen initially
        showLogin();
    }
    public void switchToSettingsView(User user) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SettingsView.fxml"));
        Parent root = loader.load();

        // Pass current user to the controller
        SettingsController controller = loader.getController();
        controller.setCurrentUserId(user.getId());
        controller.setSceneRouter(this); // pass SceneRouter reference

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();

    } catch (IOException e) {
        e.printStackTrace();
        showAlert("Error", "Failed to load settings view: " + e.getMessage(), Alert.AlertType.ERROR);
    }
    }

    public void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set the router reference
            LoginController controller = loader.getController();
            controller.setSceneRouter(this);
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
            
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load login view: " + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }
    
    public void showDashboard() {
        if (currentUser == null) {
            showAlert("Error", "No user logged in", AlertType.ERROR);
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DashboardView.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set references
            DashboardController controller = loader.getController();
            controller.setSceneRouter(this);
            controller.setCurrentUser(currentUser);
            controller.setTaskController(taskController);
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
            
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load dashboard view: " + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }
    
    public void logout() {
        currentUser = null;
        taskController.setCurrentUser(null);
        showLogin();
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        this.taskController.setCurrentUser(user);
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public TaskController getTaskController() {
        return taskController;
    }
    
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    
    private void applyStyles() {
        // This method can be used to apply global styles or themes
        // For now, we'll rely on CSS files
    }
    
    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
