package com.taskmanager.controller;

import com.taskmanager.service.DatabaseService;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

public class SettingsController {

    @FXML
    private ComboBox<String> priorityComboBox;

    @FXML
    private Button backButton;

    private SceneRouter sceneRouter;

    public void setSceneRouter(SceneRouter router) {
        this.sceneRouter = router;
    }

    @FXML
    private void goBack() {
        if (sceneRouter != null) {
            sceneRouter.showDashboard();
        }
    }

    private DatabaseService db = DatabaseService.getInstance();

    // You need to set this from your login/session
    private int currentUserId;

    /**
     * Initialize method called by JavaFX after FXML is loaded.
     * Loads the current preference into the ComboBox.
     */
    @FXML
    public void initialize() {
        // Populate ComboBox options
        priorityComboBox.getItems().addAll("High", "Medium", "Low");

        // Load current user preference
        if (currentUserId != 0) {
            String pref = db.getUserPreference(currentUserId, "defaultTaskPriority");
            if (pref != null) {
                priorityComboBox.setValue(pref);
            } else {
                priorityComboBox.setValue("Medium"); // default fallback
            }
        }
    }

    /**
     * Save the selected preference to the database.
     */
    @FXML
    public void savePreference() {
    String selectedPriority = priorityComboBox.getValue();
    if (selectedPriority != null && currentUserId != 0) {
        db.saveUserPreference(currentUserId, "defaultTaskPriority", selectedPriority);
        System.out.println("Preference saved: " + selectedPriority);
    }
    }


    /**
     * Go back to the dashboard. Assumes current stage has dashboard loaded elsewhere.
     */


    /**
     * Setter for current user ID, should be called after login
     */
    public void setCurrentUserId(int userId) {
    this.currentUserId = userId;
    loadPreferences();
    }

    private void loadPreferences() {
        if (currentUserId != 0) {
            String pref = db.getUserPreference(currentUserId, "defaultTaskPriority");
            priorityComboBox.setValue(pref != null ? pref : "Medium");
        }
    }


    
}
