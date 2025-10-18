package com.taskmanager;

import com.taskmanager.config.ConfigManager;
import com.taskmanager.controller.SceneRouter;
import com.taskmanager.service.DatabaseService;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize configuration
            ConfigManager configManager = ConfigManager.getInstance();
            configManager.printConfigStatus();
            
            // Initialize database service
            DatabaseService databaseService = DatabaseService.getInstance();
            
            // Initialize scene router and show the application
            SceneRouter sceneRouter = SceneRouter.getInstance();
            sceneRouter.init(primaryStage);
            
            // Set application icon (optional)
            // primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app-icon.png")));
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to start application: " + e.getMessage());
        }
    }

    @Override
    public void stop() throws Exception {
        // Clean up resources when application is closed
        DatabaseService.getInstance().close();
        super.stop();
    }

    public static void main(String[] args) {
        // Set system properties for better performance
        System.setProperty("javafx.preloader", "com.taskmanager.Preloader");
        
        // Launch the JavaFX application
        launch(args);
    }
}
