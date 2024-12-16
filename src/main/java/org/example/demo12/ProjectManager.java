package org.example.demo12;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ProjectManager {

    private final DatabaseManager databaseManager;
    private final ObservableList<Project> projectList;

    public ProjectManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.projectList = FXCollections.observableArrayList();
    }

    public void openManageProjectsWindow() {
        Stage manageStage = new Stage();
        manageStage.setTitle("Manage Projects");

        VBox layout = new VBox(10);
        layout.setPadding(HelperClass.getDefaultPadding());

        // Создаем таблицу проектов
        TableView<Project> projectTable = new TableView<>();
        TableColumn<Project, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());

        TableColumn<Project, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(data -> data.getValue().descriptionProperty());

        TableColumn<Project, String> deadlineColumn = new TableColumn<>("Deadline");
        deadlineColumn.setCellValueFactory(data -> data.getValue().deadlineProperty());

        // Новая колонка для отображения связанного сотрудника
        TableColumn<Project, String> employeeColumn = new TableColumn<>("Assigned Employee");
        employeeColumn.setCellValueFactory(data -> {
            int employeeId = data.getValue().getEmployeeId();
            return new ReadOnlyStringWrapper(getEmployeeNameById(employeeId));
        });

        // Добавляем столбцы в таблицу
        projectTable.getColumns().addAll(nameColumn, descriptionColumn, deadlineColumn, employeeColumn);
        projectTable.setItems(projectList);

        // Кнопки для управления проектами
        Button addButton = HelperClass.createStyledButton("Add Project", e -> openAddEditProjectWindow(null));
        Button editButton = HelperClass.createStyledButton("Edit Project", e -> {
            Project selectedProject = projectTable.getSelectionModel().getSelectedItem();
            if (selectedProject != null) {
                openAddEditProjectWindow(selectedProject);
            }
        });
        Button deleteButton = HelperClass.createStyledButton("Delete Project", e -> {
            Project selectedProject = projectTable.getSelectionModel().getSelectedItem();
            if (selectedProject != null) {
                deleteProject(selectedProject);
            }
        });

        HBox buttonLayout = new HBox(10, addButton, editButton, deleteButton);
        layout.getChildren().addAll(projectTable, buttonLayout);

        manageStage.setScene(new Scene(layout, 600, 400));
        manageStage.show();

        loadProjects(); // Загрузка данных проектов
    }

    private String getEmployeeNameById(int employeeId) {
        if (employeeId <= 0) {
            return "Unassigned"; // Если сотрудник не привязан
        }
        String query = "SELECT name FROM employees WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, employeeId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown"; // Если что-то пошло не так
    }

    private void showAlert(String message) {
        // Создаем всплывающее окно с типом WARNING
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null); // Без заголовка
        alert.setContentText(message);

        // Показываем окно и ждем, пока пользователь закроет его, чтобы продолжить
        alert.showAndWait();
    }

    private void openAddEditProjectWindow(Project project) {
        Stage addEditStage = new Stage();
        addEditStage.setTitle(project == null ? "Add Project" : "Edit Project");

        VBox layout = new VBox(10);
        layout.setPadding(HelperClass.getDefaultPadding());

        TextField nameField = new TextField();
        nameField.setPromptText("Project Name");

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");

        DatePicker deadlinePicker = new DatePicker();
        deadlinePicker.setPromptText("Select Deadline");

        // ComboBox для выбора сотрудника
        ComboBox<Employee> employeeComboBox = new ComboBox<>();
        employeeComboBox.setPromptText("Select Employee");
        ObservableList<Employee> employeeList = FXCollections.observableArrayList();
        loadEmployees(employeeList); // Загрузка сотрудников из БД
        employeeComboBox.setItems(employeeList);

        if (project != null) {
            nameField.setText(project.getName());
            descriptionField.setText(project.getDescription());

            if (project.getDeadline() != null && !project.getDeadline().isEmpty()) {
                deadlinePicker.setValue(java.time.LocalDate.parse(project.getDeadline()));
            }

            if (project.getEmployeeId() > 0) {
                employeeList.stream()
                        .filter(employee -> employee.getId() == project.getEmployeeId())
                        .findFirst()
                        .ifPresent(employeeComboBox::setValue);
            }
        }

        Button saveButton = HelperClass.createStyledButton("Save", e -> {
            String name = nameField.getText();
            String description = descriptionField.getText();
            java.time.LocalDate deadline = deadlinePicker.getValue();
            Employee selectedEmployee = employeeComboBox.getValue();

            if (selectedEmployee == null) {
                showAlert("Please select an employee!");
                return;
            }

            if (deadline == null) {
                showAlert("Please select a valid deadline!");
                return;
            }

            String deadlineString = deadline.toString();
            int employeeId = selectedEmployee.getId();

            if (project == null) {
                addProject(name, description, deadlineString, employeeId);
            } else {
                updateProject(project, name, description, deadlineString, employeeId);
            }
            addEditStage.close();
        });

        Button cancelButton = HelperClass.createStyledButton("Cancel", e -> addEditStage.close());

        layout.getChildren().addAll(nameField, descriptionField, deadlinePicker, employeeComboBox, saveButton, cancelButton);

        addEditStage.setScene(new Scene(layout, 300, 300));
        addEditStage.show();
    }

    private void loadEmployees(ObservableList<Employee> employeeList) {
        try (Connection connection = databaseManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM employees")) {

            while (resultSet.next()) {
                employeeList.add(new Employee(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("position"),
                        resultSet.getString("contact")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadProjects() {
        projectList.clear();
        try (Connection connection = databaseManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM projects")) {

            while (resultSet.next()) {
                projectList.add(new Project(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getString("deadline"),
                        resultSet.getInt("employee_id") // Новый столбец
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private void addProject(String name, String description, String deadline, int employeeId) {
        String query = "INSERT INTO projects (name, description, deadline, employee_id) VALUES (?, ?, ?, ?)";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, name);
            statement.setString(2, description);
            statement.setString(3, deadline);
            statement.setInt(4, employeeId);
            statement.executeUpdate();

            loadProjects(); // Refresh the project list
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateProject(Project project, String name, String description, String deadline, int employeeId) {
        String query = "UPDATE projects SET name = ?, description = ?, deadline = ?, employee_id = ? WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, name);
            statement.setString(2, description);
            statement.setString(3, deadline);
            statement.setInt(4, employeeId);
            statement.setInt(5, project.getId());
            statement.executeUpdate();

            loadProjects(); // Refresh the project list
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateProject(Project project, String name, String description, String deadline) {
        String query = "UPDATE projects SET name = ?, description = ?, deadline = ? WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, name);
            statement.setString(2, description);
            statement.setString(3, deadline);
            statement.setInt(4, project.getId());
            statement.executeUpdate();

            loadProjects(); // Refresh the project list
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteProject(Project project) {
        String query = "DELETE FROM projects WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, project.getId());
            statement.executeUpdate();

            loadProjects(); // Refresh the project list
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}