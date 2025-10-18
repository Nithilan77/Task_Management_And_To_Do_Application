package com.taskmanager.controller;

import com.taskmanager.entity.User;
import com.taskmanager.service.DatabaseService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;

public class LoginController {
    
    @FXML
    private TextField emailField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Hyperlink registerLink;
    
    @FXML
    private Label statusLabel;
    
    private SceneRouter sceneRouter;
    private final DatabaseService databaseService;
    
    public LoginController() {
        this.databaseService = DatabaseService.getInstance();
    }
    
    public void setSceneRouter(SceneRouter sceneRouter) {
        this.sceneRouter = sceneRouter;
    }
    
    @FXML
    private void initialize() {
        // Set up event handlers
        emailField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> onLogin());
        
        // Clear status label when user starts typing
        emailField.textProperty().addListener((obs, oldVal, newVal) -> clearStatus());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> clearStatus());
    }
    
    @FXML
    private void onLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        if (email.isEmpty() || password.isEmpty()) {
            showStatus("Please enter both email and password", true);
            return;
        }
        
        try {
            User user = databaseService.authenticateUser(email, password);
            if (user != null) {
                sceneRouter.setCurrentUser(user);
                sceneRouter.showDashboard();
            } else {
                showStatus("Invalid email or password", true);
            }
        } catch (Exception e) {
            showStatus("Login failed: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }
    
    @FXML
    private void onRegister() {
        showRegisterDialog();
    }
    
    private void showRegisterDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Create Account");
        dialog.setHeaderText("Enter your details to create a new account");
        
        // Set the button types
        ButtonType registerButtonType = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);
        
        // Create the registration form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");
        TextField displayNameField = new TextField();
        displayNameField.setPromptText("Display Name");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        
        grid.add(new Label("Email:"), 0, 0);
        grid.add(emailField, 1, 0);
        grid.add(new Label("Display Name:"), 0, 1);
        grid.add(displayNameField, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(new Label("Confirm Password:"), 0, 3);
        grid.add(confirmPasswordField, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the email field by default
        emailField.requestFocus();
        
        // Convert the result to a User object when the register button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registerButtonType) {
                String email = emailField.getText().trim();
                String displayName = displayNameField.getText().trim();
                String password = passwordField.getText();
                String confirmPassword = confirmPasswordField.getText();
                
                if (email.isEmpty() || displayName.isEmpty() || password.isEmpty()) {
                    showAlert("Error", "All fields are required", AlertType.ERROR);
                    return null;
                }
                
                if (!password.equals(confirmPassword)) {
                    showAlert("Error", "Passwords do not match", AlertType.ERROR);
                    return null;
                }
                
                if (password.length() < 6) {
                    showAlert("Error", "Password must be at least 6 characters long", AlertType.ERROR);
                    return null;
                }
                
                try {
                    User user = databaseService.registerUser(email, password, displayName);
                    showAlert("Success", "Account created successfully! You can now log in.", AlertType.INFORMATION);
                    return user;
                } catch (Exception e) {
                    showAlert("Error", "Registration failed: " + e.getMessage(), AlertType.ERROR);
                    return null;
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }
    
    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        if (isError) {
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        } else {
            statusLabel.setStyle("-fx-text-fill: #27ae60;");
        }
    }
    
    private void clearStatus() {
        statusLabel.setVisible(false);
    }
    
    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
