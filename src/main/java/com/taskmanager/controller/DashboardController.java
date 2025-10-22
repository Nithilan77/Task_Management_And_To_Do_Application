package com.taskmanager.controller;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Callback;

public class DashboardController implements Initializable {
    
    @FXML
    private Text welcomeText;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> priorityFilter;
    
    @FXML
    private ComboBox<String> statusFilter;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Text totalTasksText;
    
    @FXML
    private Text completedTasksText;
    
    @FXML
    private Text pendingTasksText;
    
    @FXML
    private Text highPriorityTasksText;
    
    @FXML
    private Text overdueTasksText;
    
    @FXML
    private Button addTaskButton;
    
    @FXML
    private Button clearCompletedButton;
    
    @FXML
    private TableView<Task> taskTable;
    
    @FXML
    private TableColumn<Task, Boolean> completedColumn;
    
    @FXML
    private TableColumn<Task, String> titleColumn;
    
    @FXML
    private TableColumn<Task, String> descriptionColumn;
    
    @FXML
    private TableColumn<Task, String> priorityColumn;
    
    @FXML
    private TableColumn<Task, String> deadlineColumn;
    
    @FXML
    private TableColumn<Task, String> createdColumn;
    
    @FXML
    private TableColumn<Task, String> actionsColumn;
    
    private User currentUser;
    private TaskController taskController;
    private SceneRouter sceneRouter;
    private ObservableList<Task> tasks;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupEventHandlers();
        setupFilters();
    }
    
    @FXML
    private void onSettings() {
        if (sceneRouter != null && currentUser != null) {
            sceneRouter.switchToSettingsView(currentUser);
        }
    }


    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            welcomeText.setText("Welcome, " + user.getDisplayName() + "!");
        }
    }
    
    public void setTaskController(TaskController taskController) {
        this.taskController = taskController;
        this.tasks = taskController.getTasks();
        taskTable.setItems(tasks);
        updateStats();
    }
    
    public void setSceneRouter(SceneRouter sceneRouter) {
        this.sceneRouter = sceneRouter;
    }
    
    private void setupTableColumns() {
        // Completed column with checkbox
        completedColumn.setCellValueFactory(new PropertyValueFactory<>("completed"));
        completedColumn.setCellFactory(CheckBoxTableCell.forTableColumn(completedColumn));
        completedColumn.setOnEditCommit(event -> {
            Task task = event.getRowValue();
            taskController.toggleTaskCompletion(task);
            updateStats();
        });
        
        // Title column
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        
        // Description column
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        // Priority column with color coding
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        priorityColumn.setCellFactory(column -> new TableCell<Task, String>() {
            @Override
            protected void updateItem(String priority, boolean empty) {
                super.updateItem(priority, empty);
                if (empty || priority == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(priority);
                    switch (priority) {
                        case "High":
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            break;
                        case "Medium":
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                            break;
                        case "Low":
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            break;
                    }
                }
            }
        });
        
        // Deadline column
        deadlineColumn.setCellValueFactory(cellData -> {
            LocalDate deadline = cellData.getValue().getDeadline();
            if (deadline != null) {
                return new SimpleStringProperty(deadline.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            }
            return new SimpleStringProperty("");
        });
        
        // Created column
        createdColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                return new SimpleStringProperty(cellData.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            }
            return new SimpleStringProperty("");
        });
        
        // Actions column
        actionsColumn.setCellFactory(createActionsCellFactory());
    }
    
    private Callback<TableColumn<Task, String>, TableCell<Task, String>> createActionsCellFactory() {
        return column -> new TableCell<Task, String>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            
            {
                editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5 10;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5 10;");
                
                editButton.setOnAction(event -> {
                    Task task = getTableView().getItems().get(getIndex());
                    showEditTaskDialog(task);
                });
                
                deleteButton.setOnAction(event -> {
                    Task task = getTableView().getItems().get(getIndex());
                    showDeleteConfirmation(task);
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, editButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        };
    }
    
    private void setupEventHandlers() {
        // Search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            taskController.searchTasks(newVal);
            updateStats();
        });
        
        // Filter functionality
        priorityFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals("All")) {
                taskController.loadTasksByPriority(newVal);
            } else {
                taskController.loadUserTasks();
            }
            updateStats();
        });
        
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals("All")) {
                boolean completed = newVal.equals("Completed");
                taskController.loadTasksByStatus(completed);
            } else {
                taskController.loadUserTasks();
            }
            updateStats();
        });
    }
    
    private void setupFilters() {
        // Setup priority filter
        priorityFilter.getItems().addAll("All", "High", "Medium", "Low");
        priorityFilter.setValue("All");
        
        // Setup status filter
        statusFilter.getItems().addAll("All", "Pending", "Completed");
        statusFilter.setValue("All");
    }
    
    @FXML
    private void onLogout() {
        sceneRouter.logout();
    }
    
    @FXML
    private void onRefresh() {
        taskController.loadUserTasks();
        updateStats();
        clearFilters();
    }
    
    @FXML
    private void onAddTask() {
        showAddTaskDialog();
    }
    
    @FXML
    private void onClearCompleted() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Clear Completed Tasks");
        alert.setHeaderText("Are you sure you want to clear all completed tasks?");
        alert.setContentText("This action cannot be undone.");
        
        if (alert.showAndWait().orElse(null) == ButtonType.OK) {
            taskController.clearCompletedTasks();
            updateStats();
        }
    }
    
    private void showAddTaskDialog() {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Add New Task");
        dialog.setHeaderText("Enter task details");
        
        // Apply dialog styling
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("dialog-pane");
        
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new javafx.geometry.Insets(20));
        
        TextField titleField = new TextField();
        titleField.setPromptText("Task Title");
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Task Description");
        descriptionField.setPrefRowCount(3);
        ComboBox<String> priorityField = new ComboBox<>();
        priorityField.getItems().addAll("High", "Medium", "Low");
        priorityField.setValue("Medium");
        DatePicker deadlineField = new DatePicker();
        deadlineField.setValue(LocalDate.now().plusDays(7));
        
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Priority:"), 0, 2);
        grid.add(priorityField, 1, 2);
        grid.add(new Label("Deadline:"), 0, 3);
        grid.add(deadlineField, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        titleField.requestFocus();
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                String title = titleField.getText().trim();
                String description = descriptionField.getText().trim();
                String priority = priorityField.getValue();
                LocalDate deadline = deadlineField.getValue();
                
                if (title.isEmpty()) {
                    showAlert("Error", "Task title is required", AlertType.ERROR);
                    return null;
                }
                
                taskController.addTask(title, description, priority, deadline);
                updateStats();
                return null;
            }
            return null;
        });
        
        dialog.showAndWait();
    }
    
    private void showEditTaskDialog(Task task) {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Edit Task");
        dialog.setHeaderText("Update task details");
        
        // Apply dialog styling
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("dialog-pane");
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new javafx.geometry.Insets(20));
        
        TextField titleField = new TextField(task.getTitle());
        TextArea descriptionField = new TextArea(task.getDescription());
        descriptionField.setPrefRowCount(3);
        ComboBox<String> priorityField = new ComboBox<>();
        priorityField.getItems().addAll("High", "Medium", "Low");
        priorityField.setValue(task.getPriority());
        DatePicker deadlineField = new DatePicker(task.getDeadline());
        CheckBox completedField = new CheckBox("Completed");
        completedField.setSelected(task.isCompleted());
        
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Priority:"), 0, 2);
        grid.add(priorityField, 1, 2);
        grid.add(new Label("Deadline:"), 0, 3);
        grid.add(deadlineField, 1, 3);
        grid.add(new Label("Completed:"), 0, 4);
        grid.add(completedField, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        titleField.requestFocus();
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String title = titleField.getText().trim();
                String description = descriptionField.getText().trim();
                String priority = priorityField.getValue();
                LocalDate deadline = deadlineField.getValue();
                boolean completed = completedField.isSelected();
                
                if (title.isEmpty()) {
                    showAlert("Error", "Task title is required", AlertType.ERROR);
                    return null;
                }
                
                task.setTitle(title);
                task.setDescription(description);
                task.setPriority(priority);
                task.setDeadline(deadline);
                task.setCompleted(completed);
                
                taskController.updateTask(task);
                updateStats();
                return null;
            }
            return null;
        });
        
        dialog.showAndWait();
    }
    
    private void showDeleteConfirmation(Task task) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Delete Task");
        alert.setHeaderText("Are you sure you want to delete this task?");
        alert.setContentText("Task: " + task.getTitle() + "\nThis action cannot be undone.");
        
        if (alert.showAndWait().orElse(null) == ButtonType.OK) {
            taskController.deleteTask(task);
            updateStats();
        }
    }
    
    private void updateStats() {
        totalTasksText.setText(String.valueOf(taskController.getTaskCount()));
        completedTasksText.setText(String.valueOf(taskController.getCompletedTaskCount()));
        pendingTasksText.setText(String.valueOf(taskController.getPendingTaskCount()));
        highPriorityTasksText.setText(String.valueOf(taskController.getHighPriorityTaskCount()));
        overdueTasksText.setText(String.valueOf(taskController.getOverdueTaskCount()));
    }
    
    private void clearFilters() {
        searchField.clear();
        priorityFilter.setValue("All");
        statusFilter.setValue("All");
    }
    
    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Apply dialog styling
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("dialog-pane");
        
        alert.showAndWait();
    }
}
