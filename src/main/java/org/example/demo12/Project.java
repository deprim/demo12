package org.example.demo12;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Project {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty name;
    private final SimpleStringProperty description;
    private final SimpleStringProperty deadline;
    private final SimpleIntegerProperty employeeId;

    public Project(int id, String name, String description, String deadline, int employeeId) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.description = new SimpleStringProperty(description);
        this.deadline = new SimpleStringProperty(deadline);
        this.employeeId = new SimpleIntegerProperty(employeeId);
    }

    // Getters for Properties (для доступа к привязываемым свойствам в TableView)
    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public SimpleStringProperty deadlineProperty() {
        return deadline;
    }

    public SimpleIntegerProperty employeeIdProperty() {
        return employeeId;
    }

    // Getters for the underlying values (если нужно получить обычные значения)
    public int getId() {
        return id.get();
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

    public int getEmployeeId() {
        return employeeId.get();
    }

    // Setters для изменения данных
    public void setName(String name) {
        this.name.set(name);
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public void setDeadline(String deadline) {
        this.deadline.set(deadline);
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId.set(employeeId);
    }
}