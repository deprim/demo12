package org.example.demo12;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private Connection connection;

    public void initializeDatabase() {
        try {
            // Establish initial connection to SQLite database
            connection = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");

            // Create all necessary tables
            createUsersTable();
            createEmployeesTable();
            createProjectsTable();


        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize the database", e);
        }
    }

    public Connection getConnection() throws SQLException {
        // Ensure the connection is valid before returning
        if (connection == null || connection.isClosed()) {
            // Reinitialize the connection if it has been closed or not created
            connection = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
        }
        return connection;
    }

    public void closeDatabase() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createUsersTable() {
        String createUsersTableSQL = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                role TEXT NOT NULL
            );
        """;

        try (Statement statement = getConnection().createStatement()) {
            statement.execute(createUsersTableSQL);
            System.out.println("Users table created (if it did not already exist).");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create users table", e);
        }
    }

    private void createEmployeesTable() {
        String createEmployeesTableSQL = """
            CREATE TABLE IF NOT EXISTS employees (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                position TEXT NOT NULL,
                contact TEXT NOT NULL
            );
        """;

        try (Statement statement = getConnection().createStatement()) {
            statement.execute(createEmployeesTableSQL);
            System.out.println("Employees table created (if it did not already exist).");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create employees table", e);
        }
    }

    // New method to create projects table
    private void createProjectsTable() {
        String createProjectsTableSQL = """
        CREATE TABLE IF NOT EXISTS projects (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            description TEXT,
            deadline TEXT
        );
    """;

        String addEmployeeIdColumnSQL = """
        ALTER TABLE projects ADD COLUMN employee_id INTEGER;
    """;

        try (Statement statement = getConnection().createStatement()) {
            // Создаем таблицу, если она еще не существует
            statement.execute(createProjectsTableSQL);
            System.out.println("Projects table created (if it did not already exist).");

            // Проверяем наличие столбца employee_id в таблице projects
            if (!columnExists("projects", "employee_id")) {
                // Если столбца employee_id нет, добавляем его
                statement.execute(addEmployeeIdColumnSQL);
                System.out.println("employee_id column added to projects table.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create or update projects table", e);
        }
    }

    private boolean columnExists(String tableName, String columnName) {
        String query = "PRAGMA table_info(" + tableName + ");";

        try (Statement statement = getConnection().createStatement();
             var resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String existingColumnName = resultSet.getString("name");
                if (columnName.equalsIgnoreCase(existingColumnName)) {
                    return true; // Столбец уже существует
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Столбец не найден
    }


}