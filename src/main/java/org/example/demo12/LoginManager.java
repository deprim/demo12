package org.example.demo12;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginManager {

    private final DatabaseManager databaseManager;

    public LoginManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public String showLoginWindow() {
        Stage loginStage = new Stage();
        loginStage.setTitle("Login");

        VBox layout = new VBox(10);
        layout.setPadding(HelperClass.getDefaultPadding());
        layout.setBackground(HelperClass.getBackground(Color.LIGHTGRAY));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Label statusLabel = new Label();
        statusLabel.setTextFill(Color.RED);

        Button loginButton = HelperClass.createStyledButton("Login", e -> {
            String role = authenticateUser(usernameField.getText(), passwordField.getText());
            if (role != null) {
                loginStage.close();
            } else {
                statusLabel.setText("Invalid credentials. Try again.");
            }
        });

        layout.getChildren().addAll(usernameField, passwordField, loginButton, statusLabel);

        Scene scene = new Scene(layout, 300, 200);
        loginStage.setScene(scene);
        loginStage.showAndWait();

        return loginStage.isShowing() ? null : authenticateUser(usernameField.getText(), passwordField.getText());
    }

    public void openRegistrationWindow() {
        Stage registrationStage = new Stage();
        registrationStage.setTitle("User Registration");

        VBox layout = new VBox(10);
        layout.setPadding(HelperClass.getDefaultPadding());

        // Поля для ввода данных
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");

        TextField positionField = new TextField();
        positionField.setPromptText("Position");

        TextField contactField = new TextField();
        contactField.setPromptText("Contact");

        Label statusLabel = new Label();
        statusLabel.setTextFill(Color.RED);

        // Кнопка регистрации
        Button registerButton = HelperClass.createStyledButton("Register", e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String name = nameField.getText();
            String position = positionField.getText();
            String contact = contactField.getText();

            if (!username.isEmpty() && !password.isEmpty() && !name.isEmpty() && !position.isEmpty() && !contact.isEmpty()) {
                if (registerNewUser(username, password, name, position, contact)) {
                    statusLabel.setTextFill(Color.GREEN);
                    statusLabel.setText("Registration successful!");
                    registrationStage.close();
                } else {
                    statusLabel.setText("Username already exists. Try a different one.");
                }
            } else {
                statusLabel.setText("All fields must be filled.");
            }
        });

        Button cancelButton = HelperClass.createStyledButton("Cancel", e -> registrationStage.close());

        layout.getChildren().addAll(usernameField, passwordField, nameField, positionField, contactField, registerButton, cancelButton, statusLabel);

        registrationStage.setScene(new Scene(layout, 300, 350));
        registrationStage.show();
    }

    private boolean registerNewUser(String username, String password, String name, String position, String contact) {
        String checkQuery = "SELECT id FROM employees WHERE username = ?";
        String insertQuery = "INSERT INTO employees (username, password, name, position, contact) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
             PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {

            // Проверяем уникальность логина
            checkStmt.setString(1, username);
            ResultSet resultSet = checkStmt.executeQuery();
            if (resultSet.next()) {
                return false; // Логин уже существует
            }

            // Добавляем пользователя
            insertStmt.setString(1, username);
            insertStmt.setString(2, password); // Здесь можно использовать хэш для пароля
            insertStmt.setString(3, name);
            insertStmt.setString(4, position);
            insertStmt.setString(5, contact);
            insertStmt.executeUpdate();

            return true; // Успешная регистрация
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String authenticateUser(String username, String password) {
        try {
            Connection connection = databaseManager.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT role FROM users WHERE username = ? AND password = ?");
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}