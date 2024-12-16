package org.example.demo12;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.sql.*;

public class MainApp extends Application {

    private Connection connection;
    private TableView<Employee> employeeTable;
    private TableView<Project> projectTable;
    private ObservableList<Employee> employeeList;
    private ObservableList<Project> projectList;
    private ObservableList<String> assignedEmployees;
    private String currentUserRole;

    @Override
    public void start(Stage primaryStage) {
        // Initialize database connection
        initializeDatabase();

        // Login window
        if (!showLoginWindow()) {
            System.exit(0);
        }

        // Main window setup
        primaryStage.setTitle("Employee and Project Management System");

        // Main layout
        VBox mainLayout = new VBox();
        mainLayout.setSpacing(10);
        mainLayout.setPadding(new Insets(15));
        mainLayout.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));

        Label headerLabel = new Label("Employee and Project Management System");
        headerLabel.setFont(new Font("Arial", 20));
        headerLabel.setTextFill(Color.DARKBLUE);

        // Navigation buttons
        Button manageEmployeesButton = createStyledButton("Manage Employees");
        Button manageProjectsButton = createStyledButton("Manage Projects");
        Button generateReportsButton = createStyledButton("Generate Reports");
        Button helpButton = createStyledButton("Help");
        Button exitButton = createStyledButton("Exit");

        // Button actions
        manageEmployeesButton.setOnAction(e -> openManageEmployeesWindow());
        manageProjectsButton.setOnAction(e -> openManageProjectsWindow());
        generateReportsButton.setOnAction(e -> generateReports());
        helpButton.setOnAction(e -> openHelpWindow());
        exitButton.setOnAction(e -> primaryStage.close());

        // Role-based access control
        if (!"Admin".equals(currentUserRole)) {
            manageEmployeesButton.setDisable(true);
            generateReportsButton.setDisable(true);
        }

        mainLayout.getChildren().addAll(headerLabel, manageEmployeesButton, manageProjectsButton, generateReportsButton, helpButton, exitButton);

        // Main scene
        Scene mainScene = new Scene(mainLayout, 500, 400);
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    // Styled button helper method
    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setFont(new Font("Arial", 14));
        button.setTextFill(Color.WHITE);
        button.setBackground(new Background(new BackgroundFill(Color.DARKBLUE, new CornerRadii(5), Insets.EMPTY)));
        button.setPadding(new Insets(10, 20, 10, 20));
        button.setOnMouseEntered(e -> button.setBackground(new Background(new BackgroundFill(Color.DARKCYAN, new CornerRadii(5), Insets.EMPTY))));
        button.setOnMouseExited(e -> button.setBackground(new Background(new BackgroundFill(Color.DARKBLUE, new CornerRadii(5), Insets.EMPTY))));
        return button;
    }

    // Show login window
    private boolean showLoginWindow() {
        Stage loginStage = new Stage();
        loginStage.setTitle("Login");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        layout.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Label statusLabel = new Label();
        statusLabel.setTextFill(Color.RED);

        Button loginButton = createStyledButton("Login");
        loginButton.setOnAction(e -> {
            String role = authenticateUser(usernameField.getText(), passwordField.getText());
            if (role != null) {
                currentUserRole = role;
                loginStage.close();
            } else {
                statusLabel.setText("Invalid credentials. Try again.");
            }
        });

        layout.getChildren().addAll(usernameField, passwordField, loginButton, statusLabel);

        Scene scene = new Scene(layout, 300, 200);
        loginStage.setScene(scene);
        loginStage.showAndWait();

        return loginStage.isShowing() == false;
    }

    // Authenticate user
    private String authenticateUser(String username, String password) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT role FROM users WHERE username = ? AND password = ?");
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Open "Manage Employees" window (method added)
    private void openManageEmployeesWindow() {
        Stage manageStage = new Stage();
        manageStage.setTitle("Manage Employees");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        // Create table for displaying employees
        employeeTable = new TableView<>();
        employeeList = FXCollections.observableArrayList();

        TableColumn<Employee, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        TableColumn<Employee, String> positionColumn = new TableColumn<>("Position");
        positionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPosition()));
        TableColumn<Employee, String> contactColumn = new TableColumn<>("Contact");
        contactColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getContact()));

        employeeTable.getColumns().addAll(nameColumn, positionColumn, contactColumn);
        employeeTable.setItems(employeeList);

        // Buttons for adding, editing, and deleting employees
        Button addEmployeeButton = createStyledButton("Add Employee");
        Button editEmployeeButton = createStyledButton("Edit Employee");
        Button deleteEmployeeButton = createStyledButton("Delete Employee");

        addEmployeeButton.setOnAction(e -> openAddEditEmployeeWindow(null));
        editEmployeeButton.setOnAction(e -> {
            Employee selectedEmployee = employeeTable.getSelectionModel().getSelectedItem();
            if (selectedEmployee != null) {
                openAddEditEmployeeWindow(selectedEmployee);
            }
        });
        deleteEmployeeButton.setOnAction(e -> {
            Employee selectedEmployee = employeeTable.getSelectionModel().getSelectedItem();
            if (selectedEmployee != null) {
                deleteEmployee(selectedEmployee);
            }
        });

        // Layout for buttons
        HBox buttonLayout = new HBox(10, addEmployeeButton, editEmployeeButton, deleteEmployeeButton);

        // Layout for the entire scene
        layout.getChildren().addAll(employeeTable, buttonLayout);

        Scene scene = new Scene(layout, 600, 400);
        manageStage.setScene(scene);
        manageStage.show();

        loadEmployees(); // Load employees from database
    }

    private void loadEmployees() {
        employeeList.clear();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM employees");
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

    private void openAddEditEmployeeWindow(Employee employee) {
        Stage addEditStage = new Stage();
        addEditStage.setTitle(employee == null ? "Add Employee" : "Edit Employee");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField positionField = new TextField();
        positionField.setPromptText("Position");
        TextField contactField = new TextField();
        contactField.setPromptText("Contact");

        if (employee != null) {
            nameField.setText(employee.getName());
            positionField.setText(employee.getPosition());
            contactField.setText(employee.getContact());
        }

        Button saveButton = createStyledButton("Save");
        Button cancelButton = createStyledButton("Cancel");

        saveButton.setOnAction(e -> {
            if (employee == null) {
                addEmployee(nameField.getText(), positionField.getText(), contactField.getText());
            } else {
                updateEmployee(employee, nameField.getText(), positionField.getText(), contactField.getText());
            }
            addEditStage.close();
        });

        cancelButton.setOnAction(e -> addEditStage.close());

        layout.getChildren().addAll(nameField, positionField, contactField, saveButton, cancelButton);

        Scene scene = new Scene(layout, 300, 200);
        addEditStage.setScene(scene);
        addEditStage.show();
    }


    private void addEmployee(String name, String position, String contact) {
        String query = "INSERT INTO employees (name, position, contact) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.setString(2, position);
            statement.setString(3, contact);
            statement.executeUpdate();
            loadEmployees(); // Reload the employee list
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateEmployee(Employee employee, String name, String position, String contact) {
        String query = "UPDATE employees SET name = ?, position = ?, contact = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.setString(2, position);
            statement.setString(3, contact);
            statement.setInt(4, employee.getId());
            statement.executeUpdate();
            loadEmployees(); // Reload the employee list
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteEmployee(Employee employee) {
        String query = "DELETE FROM employees WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, employee.getId());
            statement.executeUpdate();
            loadEmployees(); // Reload the employee list
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Open "Manage Projects" window



    // Open "Assign Employees" window


    // Generate reports (method added)
    private void generateReports() {
        // You can add the code to generate reports here
        System.out.println("Generate Reports window opened.");
    }

    // Open Help window (method added)
    private void openHelpWindow() {
        // You can add the code for help window here
        System.out.println("Help window opened.");
    }

    // Load projects from database
    private void loadProjects() {
        projectList.clear();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM projects");
            while (resultSet.next()) {
                projectList.add(new Project(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getString("deadline")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Initialize database connection
    private void initializeDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:app.db");
            Statement statement = connection.createStatement();

            // Создание таблиц
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS employees (id INTEGER PRIMARY KEY, name TEXT, position TEXT, contact TEXT)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS projects (id INTEGER PRIMARY KEY, name TEXT, description TEXT, deadline TEXT)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS project_assignments (project_id INTEGER, employee_id INTEGER, PRIMARY KEY (project_id, employee_id))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY, username TEXT, password TEXT, role TEXT)");

            // Проверка существования пользователя с ролью "Admin", если нет - создаём
            PreparedStatement checkAdminStatement = connection.prepareStatement("SELECT COUNT(*) FROM users WHERE role = 'Admin'");
            ResultSet rs = checkAdminStatement.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                // Если администратора нет, добавляем его
                PreparedStatement createAdminStatement = connection.prepareStatement(
                        "INSERT INTO users (username, password, role) VALUES (?, ?, ?)");
                createAdminStatement.setString(1, "admin");
                createAdminStatement.setString(2, "adminpassword");  // Убедитесь, что храните пароли безопасно
                createAdminStatement.setString(3, "Admin");
                createAdminStatement.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}

// Employee class
class Employee {
    private final int id;
    private final String name;
    private final String position;
    private final String contact;

    public Employee(int id, String name, String position, String contact) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.contact = contact;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPosition() {
        return position;
    }

    public String getContact() {
        return contact;
    }
}

// Project class
class Project {
    private final int id;
    private final StringProperty name;
    private final StringProperty description;
    private final StringProperty deadline;

    public Project(int id, String name, String description, String deadline) {
        this.id = id;
        this.name = new SimpleStringProperty(name);
        this.description = new SimpleStringProperty(description);
        this.deadline = new SimpleStringProperty(deadline);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name.get();
    }

    public String getDescription() {
        return description.get();
    }

    public String getDeadline() {
        return deadline.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public StringProperty deadlineProperty() {
        return deadline;
    }
}
