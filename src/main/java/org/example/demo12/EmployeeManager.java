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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class EmployeeManager {

    private final DatabaseManager databaseManager;
    private final ObservableList<Employee> employeeList;

    // Делаем employeeTable полем класса, чтобы доступ был во всем классе
    private TableView<Employee> employeeTable;

    public EmployeeManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        employeeList = FXCollections.observableArrayList();
    }

    public void openManageEmployeesWindow() {
        Stage manageStage = new Stage();
        manageStage.setTitle("Manage Employees");

        VBox layout = new VBox(10);
        layout.setPadding(HelperClass.getDefaultPadding());

        // Инициализируем employeeTable как поле класса
        employeeTable = new TableView<>();
        TableColumn<Employee, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getName())); // Привязка данных

        TableColumn<Employee, String> positionColumn = new TableColumn<>("Position");
        positionColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getPosition())); // Привязка данных

        TableColumn<Employee, String> contactColumn = new TableColumn<>("Contact");
        contactColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getContact())); // Привязка данных

        employeeTable.getColumns().addAll(nameColumn, positionColumn, contactColumn);
        employeeTable.setItems(employeeList);

        Button addButton = HelperClass.createStyledButton("Add", e -> openAddEmployeeWindow());
        Button editButton = HelperClass.createStyledButton("Edit", e -> {
            Employee selectedEmployee = employeeTable.getSelectionModel().getSelectedItem();
            openEditEmployeeWindow(selectedEmployee);
        });
        Button deleteButton = HelperClass.createStyledButton("Delete", e -> {
            Employee selectedEmployee = employeeTable.getSelectionModel().getSelectedItem();
            if (selectedEmployee != null) {
                deleteEmployeeFromDatabase(selectedEmployee.getId());
                loadEmployees(employeeList);// Очищаем и перезагружаем таблицу после удаления
            } else {
                showAlert("No employee selected!");
            }
        });

        HBox buttonLayout = new HBox(10, addButton, editButton, deleteButton);

        layout.getChildren().addAll(employeeTable, buttonLayout);

        manageStage.setScene(new Scene(layout, 600, 400));
        manageStage.show();

        loadEmployees(employeeList);
    }

    private void loadEmployees(ObservableList<Employee> employeeList) {
        employeeList.clear(); // Очищаем список перед загрузкой новых данных

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

    private void openAddEmployeeWindow() {
        Stage addStage = new Stage();
        addStage.setTitle("Add Employee");

        VBox layout = new VBox(10);
        layout.setPadding(HelperClass.getDefaultPadding());

        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        TextField positionField = new TextField();
        positionField.setPromptText("Position");

        TextField contactField = new TextField();
        contactField.setPromptText("Contact");

        Button saveButton = HelperClass.createStyledButton("Save", e -> {
            String name = nameField.getText();
            String position = positionField.getText();
            String contact = contactField.getText();

            if (!name.isEmpty() && !position.isEmpty() && !contact.isEmpty()) {
                addEmployeeToDatabase(name, position, contact);
                loadEmployees(employeeList);
                addStage.close();
            } else {
                showAlert("All fields must be filled!");
            }
        });

        Button cancelButton = HelperClass.createStyledButton("Cancel", e -> addStage.close());

        HBox buttonLayout = new HBox(10, saveButton, cancelButton);
        layout.getChildren().addAll(nameField, positionField, contactField, buttonLayout);

        addStage.setScene(new Scene(layout, 300, 200));
        addStage.show();
    }

    private void addEmployeeToDatabase(String name, String position, String contact) {
        String insertSQL = "INSERT INTO employees (name, position, contact) VALUES (?, ?, ?)";
        try (Connection connection = databaseManager.getConnection();
             var preparedStatement = connection.prepareStatement(insertSQL)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, position);
            preparedStatement.setString(3, contact);
            preparedStatement.executeUpdate();
            System.out.println("Added employee: " + name);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Failed to add employee: " + e.getMessage());
        }
    }

    private void openEditEmployeeWindow(Employee employee) {
        if (employee == null) {
            showAlert("No employee selected!");
            return;
        }

        Stage editStage = new Stage();
        editStage.setTitle("Edit Employee");

        VBox layout = new VBox(10);
        layout.setPadding(HelperClass.getDefaultPadding());

        TextField nameField = new TextField(employee.getName());
        TextField positionField = new TextField(employee.getPosition());
        TextField contactField = new TextField(employee.getContact());

        Button saveButton = HelperClass.createStyledButton("Save", e -> {
            String name = nameField.getText();
            String position = positionField.getText();
            String contact = contactField.getText();

            if (!name.isEmpty() && !position.isEmpty() && !contact.isEmpty()) {
                updateEmployeeInDatabase(employee.getId(), name, position, contact);
                loadEmployees(employeeList);
                editStage.close();
            } else {
                showAlert("All fields must be filled!");
            }
        });

        Button cancelButton = HelperClass.createStyledButton("Cancel", e -> editStage.close());

        HBox buttonLayout = new HBox(10, saveButton, cancelButton);
        layout.getChildren().addAll(nameField, positionField, contactField, buttonLayout);

        editStage.setScene(new Scene(layout, 300, 200));
        editStage.show();
    }

    private void updateEmployeeInDatabase(int id, String name, String position, String contact) {
        String updateSQL = "UPDATE employees SET name = ?, position = ?, contact = ? WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             var preparedStatement = connection.prepareStatement(updateSQL)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, position);
            preparedStatement.setString(3, contact);
            preparedStatement.setInt(4, id);
            preparedStatement.executeUpdate();
            System.out.println("Updated employee ID: " + id);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Failed to update employee: " + e.getMessage());
        }
    }

    private void deleteEmployeeFromDatabase(int id) {
        String deleteSQL = "DELETE FROM employees WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             var preparedStatement = connection.prepareStatement(deleteSQL)) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
            System.out.println("Deleted employee ID: " + id);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Failed to delete employee: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }
}